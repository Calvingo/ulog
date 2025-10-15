package com.ulog.backend.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.ulog.backend.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractIntegrationTest {

    private static final AtomicInteger COUNTER = new AtomicInteger(5000);

    private String uniquePhone() {
        return "+1000000" + String.format("%04d", COUNTER.getAndIncrement());
    }

    private JsonNode register(String phone, String password) throws Exception {
        String body = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"" + password + "\"," +
            "\"name\":\"Tester\"," +
            "\"smsCode\":\"123456\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String loginForToken(String phone, String password) throws Exception {
        String body = "{" +
            "\"phone\":\"" + phone + "\"," +
            "\"password\":\"" + password + "\"" +
            "}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return response.get("data").get("tokens").get("accessToken").asText();
    }

    @Test
    void shouldGetProfile() throws Exception {
        String phone = uniquePhone();
        register(phone, "Password1");
        String token = loginForToken(phone, "Password1");
        MvcResult result = mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("code").asInt()).isZero();
        assertThat(response.get("data").get("phone").asText()).isEqualTo(phone);
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        String phone = uniquePhone();
        register(phone, "Password1");
        String token = loginForToken(phone, "Password1");
        String body = "{" +
            "\"name\":\"Updated Name\"," +
            "\"description\":\"New description\"" +
            "}";
        MvcResult result = mockMvc.perform(patch("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.get("data").get("name").asText()).isEqualTo("Updated Name");
        assertThat(response.get("data").get("description").asText()).isEqualTo("New description");
    }

    @Test
    void shouldChangePassword() throws Exception {
        String phone = uniquePhone();
        register(phone, "Password1");
        String token = loginForToken(phone, "Password1");
        String body = "{" +
            "\"currentPassword\":\"Password1\"," +
            "\"newPassword\":\"Password2\"" +
            "}";
        mockMvc.perform(post("/api/v1/users/me/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        // old password should fail
        MvcResult oldLogin = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"phone\":\"" + phone + "\"," +
                    "\"password\":\"Password1\"" +
                    "}"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        JsonNode oldResponse = objectMapper.readTree(oldLogin.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(oldResponse.get("code").asInt()).isEqualTo(2004);

        // new password should succeed
        JsonNode newLogin = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"phone\":\"" + phone + "\"," +
                    "\"password\":\"Password2\"" +
                    "}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8));
        assertThat(newLogin.get("code").asInt()).isZero();
    }
}
