package com.mongodbmodfactory.kitchensink_boot.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MemberRestIT {

    private static final String BASE_URL = "/kitchensink/rest/members";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testListMembers() throws Exception {
        String name = "List Test User";
        String email = "rest" + System.currentTimeMillis() + "@test.com";
        String phoneNumber = "1234567890";
        String memberJson = String.format("""
                {
                    "name": "%s",
                    "email": "%s",
                    "phoneNumber": "%s"
                }""", name, email, phoneNumber);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJson))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", equalTo(name)))
                .andExpect(jsonPath("$[0].email", equalTo(email)))
                .andExpect(jsonPath("$[0].phoneNumber", equalTo(phoneNumber)));
    }

    @Test
    public void testCreateMember() throws Exception {
        String email = "rest" + System.currentTimeMillis() + "@test.com";
        String memberJson = String.format("""
                {
                    "name": "REST Test User",
                    "email": "%s",
                    "phoneNumber": "1234567890"
                }""", email);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("REST Test User")))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    public void testCreateInvalidMember() throws Exception {
        String memberJson = """
                {
                    "name": "Invalid User",
                    "email": "invalid-email",
                    "phoneNumber": "1234567890"
                }""";

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", containsString("must be a well-formed email address")));
    }

    @Test
    public void testCreateDuplicateEmail() throws Exception {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";
        String member1Json = String.format("""
                {
                    "name": "First User",
                    "email": "%s",
                    "phoneNumber": "1234567890"
                }""", email);

        // Create first member
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(member1Json))
                .andExpect(status().isOk());

        String member2Json = String.format("""
                {
                    "name": "Second User",
                    "email": "%s",
                    "phoneNumber": "0987654321"
                }""", email);

        // Try to create second member with same email
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(member2Json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.email", is("Email taken")));
    }

    @Test
    public void testGetMemberById() throws Exception {
        // First create a member
        String email = "getbyid" + System.currentTimeMillis() + "@test.com";
        String memberJson = String.format("""
                {
                    "name": "Get By ID Test",
                    "email": "%s",
                    "phoneNumber": "1234567890"
                }""", email);

        String responseContent = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the ID from the response using a simple substring (since we know it's valid JSON)
        String id = responseContent.split("\"id\":")[1].split(",")[0].trim();

        // Get the member by ID
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Get By ID Test")))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    public void testGetNonExistentMember() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound());
    }
}