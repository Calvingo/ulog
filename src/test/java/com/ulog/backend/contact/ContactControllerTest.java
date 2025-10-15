package com.ulog.backend.contact;

import com.fasterxml.jackson.databind.JsonNode;
import com.ulog.backend.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContactControllerTest extends AbstractIntegrationTest {

    private static final AtomicInteger COUNTER = new AtomicInteger(9000);

    private String uniquePhone() {
        return "+199900" + String.format("%04d", COUNTER.getAndIncrement());
    }

    private String registerAndToken(String phone) throws Exception {
        String registerBody = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"Password1\"," +
            "\"name\":\"Contact Owner\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        String loginBody = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"Password1\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return response.get("data").get("tokens").get("accessToken").asText();
    }

    @Test
    void shouldCreateContact() throws Exception {
        String token = registerAndToken(uniquePhone());
        String body = "{" +
            "\"name\":\"Alice\"," +
            "\"description\":\"Friend\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isZero();
        assertThat(response.get("data").get("name").asText()).isEqualTo("Alice");
    }

    @Test
    void shouldListContacts() throws Exception {
        String token = registerAndToken(uniquePhone());
        for (String name : new String[]{"Alice", "Bob"}) {
            String body = "{" +
                "\"name\":\"" + name + "\"," +
                "\"description\":\"Friend\"" +
                "}";
            mockMvc.perform(post("/api/v1/contacts")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isCreated());
        }
        MvcResult result = mockMvc.perform(get("/api/v1/contacts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("data")).isNotNull();
        assertThat(response.get("data").size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateContact() throws Exception {
        String token = registerAndToken(uniquePhone());
        String createBody = "{" +
            "\"name\":\"Alice\"" +
            "}";
        MvcResult createResult = mockMvc.perform(post("/api/v1/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        long contactId = created.get("data").get("id").asLong();

        String updateBody = "{" +
            "\"name\":\"Alice Updated\"" +
            "}";
        MvcResult updateResult = mockMvc.perform(patch("/api/v1/contacts/" + contactId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode updated = objectMapper.readTree(updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(updated.get("data").get("name").asText()).isEqualTo("Alice Updated");
    }

    @Test
    void shouldSoftDeleteContact() throws Exception {
        String token = registerAndToken(uniquePhone());
        String createBody = "{" +
            "\"name\":\"Alice\"" +
            "}";
        MvcResult createResult = mockMvc.perform(post("/api/v1/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        long contactId = created.get("data").get("id").asLong();

        mockMvc.perform(delete("/api/v1/contacts/" + contactId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/contacts/" + contactId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }
}
