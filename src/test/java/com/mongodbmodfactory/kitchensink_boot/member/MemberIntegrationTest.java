package com.mongodbmodfactory.kitchensink_boot.member;

import com.jayway.jsonpath.JsonPath;
import com.mongodbmodfactory.kitchensink_boot.helpers.TestEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ImportTestcontainers
public class MemberIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestEventListener testEventListener;

    String name = "Jane Doe";
    String email = "jane@mailinator.com";
    String phoneNumber = "2125551234";

    @BeforeEach
    void beforeEach() {
        testEventListener.reset();
    }

    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void createsMembers() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "  \"name\": \"" + name + "\"," +
                                "  \"email\": \"" + email + "\"," +
                                "  \"phoneNumber\": \"" + phoneNumber + "\"" +
                                "}"))
                .andExpect(status().isCreated());

        String membersResponseBody = mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.members", hasSize(1)))
                .andExpect(jsonPath("$._embedded.members[0].name", equalTo(name)))
                .andExpect(jsonPath("$._embedded.members[0].email", equalTo(email)))
                .andExpect(jsonPath("$._embedded.members[0].phoneNumber", equalTo(phoneNumber)))
                .andReturn().getResponse().getContentAsString();

        String memberUrl = JsonPath.parse(membersResponseBody).read("$._embedded.members[0]._links.member.href", String.class);
        mockMvc.perform(get(memberUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo(name)))
                .andExpect(jsonPath("$.email", equalTo(email)))
                .andExpect(jsonPath("$.phoneNumber", equalTo(phoneNumber)));

        assertThat(testEventListener.getAfterCreateEvents()).hasSize(1);
        Object source = testEventListener.getAfterCreateEvents().getFirst().getSource();
        assertThat(source).hasFieldOrPropertyWithValue("name", name);
        assertThat(source).hasFieldOrPropertyWithValue("email", email);
        assertThat(source).hasFieldOrPropertyWithValue("phoneNumber", phoneNumber);

        mockMvc.perform(delete(memberUrl))
                .andExpect(status().isNoContent());
    }

    @Test
    void providesErrorMessagesIfMemberCannotBeCreatedDueToValidationErrors() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "  \"name\": \"" + name + "\"," +
                                "  \"email\": \"\"," +
                                "  \"phoneNumber\": \"" + phoneNumber + "\"" +
                                "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].property", equalTo("email")))
                .andExpect(jsonPath("$.errors[0].message", equalTo("must not be empty")));
    }
}
