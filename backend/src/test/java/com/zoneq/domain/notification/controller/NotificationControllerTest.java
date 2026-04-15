package com.zoneq.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zoneq.domain.auth.dto.SignupRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private String userToken;
    private Long notificationId;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
    }

    @BeforeAll
    void setUp() throws Exception {
        MvcResult r = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("알림유저", "notif_user@test.com", "password123!"))))
                .andReturn();
        userToken = objectMapper.readTree(r.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        // Insert a notification directly into DB for this user
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'notif_user@test.com'", Long.class);
        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, type, body, is_read, created_at) VALUES (?, 'GRADE_UPDATED', '등급이 A로 변경되었습니다.', false, NOW())",
                userId);
        notificationId = jdbcTemplate.queryForObject(
                "SELECT id FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                Long.class, userId);
    }

    @Test
    @Order(1)
    void getMyNotifications_returns200_withUnreadCount() throws Exception {
        mockMvc().perform(get("/api/notifications/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications").isArray())
                .andExpect(jsonPath("$.data.unreadCount").value(1));
    }

    @Test
    @Order(2)
    void getMyNotifications_returns401_withoutToken() throws Exception {
        mockMvc().perform(get("/api/notifications/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void markAsRead_returns200() throws Exception {
        mockMvc().perform(patch("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void markAsRead_returns404_whenNotFound() throws Exception {
        mockMvc().perform(patch("/api/notifications/99999/read")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    void markAsRead_returns401_withoutToken() throws Exception {
        mockMvc().perform(patch("/api/notifications/" + notificationId + "/read"))
                .andExpect(status().isUnauthorized());
    }
}
