package com.mongodbmodfactory.kitchensink_boot.member;

import jakarta.json.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

public class MemberRestIT {

    private static final String BASE_URL = "http://localhost:8080/kitchensink/rest/members";
    private HttpClient client;

    @BeforeEach
    public void setup() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    public void testListMembers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
        JsonArray members = jsonReader.readArray();
        assertTrue(members.size() >= 0);
        jsonReader.close();
    }

    @Test
    public void testCreateMember() throws IOException, InterruptedException {
        String email = "rest" + System.currentTimeMillis() + "@test.com";
        JsonObject member = Json.createObjectBuilder()
                .add("name", "REST Test User")
                .add("email", email)
                .add("phoneNumber", "1234567890")
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(member.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testCreateInvalidMember() throws IOException, InterruptedException {
        // Test with invalid email
        JsonObject member = Json.createObjectBuilder()
                .add("name", "Invalid User")
                .add("email", "invalid-email")
                .add("phoneNumber", "1234567890")
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(member.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
        JsonObject errorResponse = jsonReader.readObject();
        assertTrue(errorResponse.containsKey("email"));
        jsonReader.close();
    }

    @Test
    public void testCreateDuplicateEmail() throws IOException, InterruptedException {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";

        // Create first member
        JsonObject member1 = Json.createObjectBuilder()
                .add("name", "First User")
                .add("email", email)
                .add("phoneNumber", "1234567890")
                .build();

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(member1.toString()))
                .build();

        client.send(request1, HttpResponse.BodyHandlers.ofString());

        // Try to create second member with same email
        JsonObject member2 = Json.createObjectBuilder()
                .add("name", "Second User")
                .add("email", email)
                .add("phoneNumber", "0987654321")
                .build();

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(member2.toString()))
                .build();

        HttpResponse<String> response = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(409, response.statusCode());

        JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
        JsonObject errorResponse = jsonReader.readObject();
        assertTrue(errorResponse.containsKey("email"));
        assertEquals("Email taken", errorResponse.getString("email"));
        jsonReader.close();
    }

    @Test
    public void testGetMemberById() throws IOException, InterruptedException {
        // First create a member
        String email = "getbyid" + System.currentTimeMillis() + "@test.com";
        JsonObject member = Json.createObjectBuilder()
                .add("name", "Get By ID Test")
                .add("email", email)
                .add("phoneNumber", "1234567890")
                .build();

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(member.toString()))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResponse.statusCode());

        // Get the member list to find the ID
        HttpRequest listRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString());
        JsonReader jsonReader = Json.createReader(new StringReader(listResponse.body()));
        JsonArray members = jsonReader.readArray();
        jsonReader.close();

        // Find the member we just created
        Long memberId = null;
        for (JsonValue memberValue : members) {
            JsonObject memberObject = memberValue.asJsonObject();
            if (memberObject.getString("email").equals(email)) {
                memberId = Long.valueOf(memberObject.getInt("id"));
                break;
            }
        }
        assertNotNull("Should find the created member's ID", memberId);

        // Get the member by ID
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + memberId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        jsonReader = Json.createReader(new StringReader(getResponse.body()));
        JsonObject retrievedMember = jsonReader.readObject();
        jsonReader.close();

        assertEquals(member.getString("name"), retrievedMember.getString("name"));
        assertEquals(member.getString("email"), retrievedMember.getString("email"));
        assertEquals(member.getString("phoneNumber"), retrievedMember.getString("phoneNumber"));
    }

    @Test
    public void testGetNonExistentMember() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}