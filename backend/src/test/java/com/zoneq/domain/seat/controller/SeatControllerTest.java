package com.zoneq.domain.seat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoneq.domain.auth.dto.SignupRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeatControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String accessToken;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeAll
    void setUpToken() throws Exception {
        SignupRequest signup = new SignupRequest("테스터", "seat_test@test.com", "password123!");
        MvcResult result = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andReturn();
        accessToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
    }

    @BeforeEach
    void setUpSeats() {
        jdbcTemplate.execute("DELETE FROM seats");
        jdbcTemplate.execute("INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('S', 1, 'AVAILABLE', CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('S', 2, 'AVAILABLE', CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('A', 1, 'AVAILABLE', CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('B', 1, 'OCCUPIED', CURRENT_TIMESTAMP)");
    }

    @Test
    void getSeats_returnsAllSeats_whenNoFilter() throws Exception {
        mockMvc().perform(get("/api/seats")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4));
    }

    @Test
    void getSeats_filtersByZone() throws Exception {
        mockMvc().perform(get("/api/seats?zone=S")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].zone").value("S"));
    }

    @Test
    void getSeats_returnsCorrectFields() throws Exception {
        mockMvc().perform(get("/api/seats?zone=B")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].zone").value("B"))
                .andExpect(jsonPath("$.data[0].seatNumber").value(1))
                .andExpect(jsonPath("$.data[0].status").value("OCCUPIED"));
    }

    @Test
    void getSeats_requires_authentication() throws Exception {
        mockMvc().perform(get("/api/seats"))
                .andExpect(status().isUnauthorized());
    }
}
