package com.ulog.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.ulog.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthValidationTest extends AbstractIntegrationTest {

    @Test
    void shouldRejectInvalidPhoneFormat() throws Exception {
        String body = "{" +
            "\"phone\":\"invalid-phone\"," +
            "\"password\":\"Password1\"," +
            "\"name\":\"Test User\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();
        
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(1001); // Validation error
    }

    @Test
    void shouldRejectWeakPassword() throws Exception {
        String body = "{" +
            "\"phone\":\"+8613800138000\"," +
            "\"password\":\"weak\"," +
            "\"name\":\"Test User\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();
        
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(1001); // Validation error
    }

    @Test
    void shouldRejectEmptyName() throws Exception {
        String body = "{" +
            "\"phone\":\"+8613800138000\"," +
            "\"password\":\"Password1\"," +
            "\"name\":\"\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();
        
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(1001); // Validation error
    }

    @Test
    void shouldRejectTooLongName() throws Exception {
        String longName = "a".repeat(65); // 超过64字符限制
        String body = "{" +
            "\"phone\":\"+8613800138000\"," +
            "\"password\":\"Password1\"," +
            "\"name\":\"" + longName + "\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();
        
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(1001); // Validation error
    }

    @Test
    void shouldRejectEmptyLoginCredentials() throws Exception {
        String body = "{" +
            "\"phone\":\"\"," +
            "\"password\":\"\"" +
            "}";
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();
        
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(1001); // Validation error
    }

    @Test
    void shouldRejectLoginWithNonExistentUser() throws Exception {
        String body = "{" +
            "\"phone\":\"+8613800138999\"," +
            "\"password\":\"Password1\"" +
            "}";
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized())
            .andReturn();
        
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(2001); // User not found
    }
}
