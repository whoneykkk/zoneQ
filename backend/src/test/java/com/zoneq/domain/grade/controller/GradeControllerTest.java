package com.zoneq.domain.grade.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradeControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private String userToken;
    private String adminToken;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
    }

    @BeforeAll
    void setUp() throws Exception {
        MvcResult r1 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("등급유저", "grade_user@test.com", "password123!"))))
                .andReturn();
        userToken = objectMapper.readTree(r1.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        MvcResult r2 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("등급관리자", "grade_admin@test.com", "password123!"))))
                .andReturn();
        adminToken = objectMapper.readTree(r2.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
        jdbcTemplate.execute(
                "UPDATE users SET role = 'ADMIN' WHERE email = 'grade_admin@test.com'");
    }

    @Test
    @Order(1)
    void getMyGrade_returns200_withNoData_whenNoSessions() throws Exception {
        mockMvc().perform(get("/api/grades/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.visitCount").value(0));
    }

    @Test
    @Order(2)
    void getMyGrade_returns401_withoutToken() throws Exception {
        mockMvc().perform(get("/api/grades/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void getMyHistory_returns200_withEmptyList_whenNoHistory() throws Exception {
        mockMvc().perform(get("/api/grades/history/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(4)
    void getUserGrade_returns200_withAdminToken() throws Exception {
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'grade_user@test.com'", Long.class);

        mockMvc().perform(get("/api/grades/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.userName").value("등급유저"));
    }

    @Test
    @Order(5)
    void getUserGrade_returns403_withUserToken() throws Exception {
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'grade_user@test.com'", Long.class);

        mockMvc().perform(get("/api/grades/users/" + userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void getDistribution_returns200_withAdminToken() throws Exception {
        mockMvc().perform(get("/api/grades/distribution")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }
}
