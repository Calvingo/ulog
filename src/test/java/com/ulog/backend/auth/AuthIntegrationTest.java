package com.ulog.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.ulog.backend.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private String uniquePhone() {
        return "+10000000" + String.format("%04d", COUNTER.getAndIncrement());
    }

    private JsonNode registerUser(String phone, String password) throws Exception {
        String body = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"" + password + "\"," +
            "\"name\":\"Test User\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode loginUser(String phone, String password, int expectedStatus) throws Exception {
        String body = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"" + password + "\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(expectedStatus))
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldRegisterUser() throws Exception {
        String phone = uniquePhone();
        JsonNode response = registerUser(phone, "Password1");
        assertThat(response.get("code").asInt()).isZero();
        assertThat(response.get("data").get("user").get("phone").asText())
            .endsWith(phone.substring(phone.length() - 4));
    }

    @Test
    void shouldNotRegisterDuplicatePhone() throws Exception {
        String phone = uniquePhone();
        registerUser(phone, "Password1");
        String body = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"Password1\"," +
            "\"name\":\"Another\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(1006);
    }

    @Test
    void shouldLoginAfterRegister() throws Exception {
        String phone = uniquePhone();
        registerUser(phone, "Password1");
        JsonNode response = loginUser(phone, "Password1", 200);
        assertThat(response.get("code").asInt()).isZero();
        assertThat(response.get("data").get("tokens").get("accessToken").asText()).isNotBlank();
    }

    @Test
    void shouldLockAfterFiveFailedAttempts() throws Exception {
        String phone = uniquePhone();
        registerUser(phone, "Password1");
        for (int i = 0; i < 5; i++) {
            JsonNode failed = loginUser(phone, "WrongPass1", 401);
            assertThat(failed.get("code").asInt()).isEqualTo(2004);
        }
        JsonNode locked = loginUser(phone, "Password1", 401);
        assertThat(locked.get("code").asInt()).isEqualTo(2005);
    }

    @Test
    void shouldRefreshToken() throws Exception {
        String phone = uniquePhone();
        registerUser(phone, "Password1");
        JsonNode login = loginUser(phone, "Password1", 200);
        String refreshToken = login.get("data").get("tokens").get("refreshToken").asText();
        String body = "{" +
            "\"refreshToken\":\"" + refreshToken + "\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isZero();
        assertThat(response.get("data").get("accessToken").asText()).isNotBlank();
    }

    @Test
    void shouldLogoutAndInvalidateRefreshToken() throws Exception {
        String phone = uniquePhone();
        registerUser(phone, "Password1");
        JsonNode login = loginUser(phone, "Password1", 200);
        String refreshToken = login.get("data").get("tokens").get("refreshToken").asText();
        String logoutBody = "{" +
            "\"refreshToken\":\"" + refreshToken + "\"" +
            "}";
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutBody))
            .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutBody))
            .andExpect(status().isUnauthorized())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isEqualTo(2003);
    }
}
