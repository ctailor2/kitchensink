package com.mongodbmodfactory.kitchensink_boot.member;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MemberIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Transactional
    void createsMembers() throws Exception {
        String name = "Jane Doe";
        String email = "jane@mailinator.com";
        String phoneNumber = "2125551234";
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
    }
}
