package com.zoneq.domain.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zoneq.domain.auth.dto.SignupRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.zoneq.domain.dashboard.service.DashboardScheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbcTemplate;
    @MockBean DashboardScheduler dashboardScheduler; // 테스트 중 스케줄러 비활성화

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private String adminToken;
    private String userToken;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
    }

    @BeforeAll
    void setUp() throws Exception {
        // USER 가입
        MvcResult r1 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("대시유저", "dash_user@test.com", "password123!"))))
                .andReturn();
        userToken = objectMapper.readTree(r1.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
        assertThat(userToken).isNotEmpty();

        // ADMIN 가입 후 role 승격
        MvcResult r2 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("대시관리자", "dash_admin@test.com", "password123!"))))
                .andReturn();
        adminToken = objectMapper.readTree(r2.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
        assertThat(adminToken).isNotEmpty();
        jdbcTemplate.execute("UPDATE users SET role = 'ADMIN' WHERE email = 'dash_admin@test.com'");
    }

    @Test
    @Order(1)
    void getStats_returns200_withAdminToken() throws Exception {
        mockMvc().perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSeats").isNumber())
                .andExpect(jsonPath("$.data.occupiedSeats").isNumber())
                .andExpect(jsonPath("$.data.warningCount").isNumber())
                .andExpect(jsonPath("$.data.gradeDistribution").exists());
    }

    @Test
    @Order(2)
    void getStats_returns403_withUserToken() throws Exception {
        mockMvc().perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void getStats_returns401_withoutToken() throws Exception {
        mockMvc().perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void getRealtime_returns200_withAdminToken() throws Exception {
        MvcResult result = mockMvc().perform(get("/api/dashboard/realtime")
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/event-stream")))
                .andReturn();
    }

    @Test
    @Order(5)
    void getRealtime_returns403_withUserToken() throws Exception {
        mockMvc().perform(get("/api/dashboard/realtime")
                        .header("Authorization", "Bearer " + userToken)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void getRealtime_returns401_withoutToken() throws Exception {
        mockMvc().perform(get("/api/dashboard/realtime")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isUnauthorized());
    }
}
