package com.zoneq.domain.noise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zoneq.domain.auth.dto.SignupRequest;
import com.zoneq.domain.noise.domain.NoiseCategory;
import com.zoneq.domain.noise.dto.CalibrationEntryRequest;
import com.zoneq.domain.noise.dto.CalibrationRequest;
import com.zoneq.domain.noise.dto.NoiseMeasurementRequest;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoiseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private String userToken;
    private String adminToken;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeAll
    void setUpUsersAndSeats() throws Exception {
        // 일반 유저 가입
        MvcResult r1 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("소음유저", "noise_user@test.com", "password123!"))))
                .andReturn();
        userToken = objectMapper.readTree(r1.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        // 관리자 가입 후 role 변경
        MvcResult r2 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("소음관리자", "noise_admin@test.com", "password123!"))))
                .andReturn();
        adminToken = objectMapper.readTree(r2.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
        jdbcTemplate.execute("UPDATE users SET role = 'ADMIN' WHERE email = 'noise_admin@test.com'");

        // 좌석 + 세션 준비
        jdbcTemplate.execute(
                "INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('B', 99, 'OCCUPIED', CURRENT_TIMESTAMP)");
        jdbcTemplate.execute(
                "UPDATE seats SET user_id = (SELECT id FROM users WHERE email = 'noise_user@test.com') WHERE seat_number = 99");
        jdbcTemplate.execute(
                "INSERT INTO sessions (user_id, seat_id, started_at, created_at) VALUES (" +
                "(SELECT id FROM users WHERE email = 'noise_user@test.com'), " +
                "(SELECT id FROM seats WHERE seat_number = 99), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
    }

    @Test
    @Order(1)
    void saveMeasurement_returns202_withValidRequest() throws Exception {
        Long seatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE seat_number = 99", Long.class);

        NoiseMeasurementRequest req = new NoiseMeasurementRequest(
                seatId, 47.3, 2, 30, NoiseCategory.KEYBOARD, LocalDateTime.now());

        mockMvc().perform(post("/api/noise/measurements")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(2)
    void saveMeasurement_returns401_withoutToken() throws Exception {
        Long seatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE seat_number = 99", Long.class);

        NoiseMeasurementRequest req = new NoiseMeasurementRequest(
                seatId, 47.3, 2, 30, NoiseCategory.KEYBOARD, LocalDateTime.now());

        mockMvc().perform(post("/api/noise/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void saveCalibration_returns200_withAdminToken() throws Exception {
        Long seatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE seat_number = 99", Long.class);

        CalibrationRequest req = new CalibrationRequest(
                List.of(new CalibrationEntryRequest(seatId, seatId, 3.5)));

        mockMvc().perform(post("/api/noise/calibration")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @Order(4)
    void saveCalibration_returns403_withUserToken() throws Exception {
        Long seatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE seat_number = 99", Long.class);

        CalibrationRequest req = new CalibrationRequest(
                List.of(new CalibrationEntryRequest(seatId, seatId, 3.5)));

        mockMvc().perform(post("/api/noise/calibration")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void getClassification_returns200_withSavedMeasurementId() throws Exception {
        Long sessionId = jdbcTemplate.queryForObject(
                "SELECT id FROM sessions WHERE user_id = (SELECT id FROM users WHERE email = 'noise_user@test.com')",
                Long.class);
        Long seatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE seat_number = 99", Long.class);

        jdbcTemplate.execute(
                "INSERT INTO noise_measurements (session_id, seat_id, leq_db, peak_count, noise_category, is_habitual, measured_at) " +
                "VALUES (" + sessionId + ", " + seatId + ", 47.3, 2, 'KEYBOARD', false, CURRENT_TIMESTAMP)");
        Long measurementId = jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM noise_measurements", Long.class);

        mockMvc().perform(get("/api/noise/classifications/" + measurementId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noiseCategory").value("KEYBOARD"))
                .andExpect(jsonPath("$.data.isHabitual").value(false))
                .andExpect(jsonPath("$.data.leqDb").value(47.3));
    }

    @Test
    @Order(6)
    void getClassification_returns404_whenNotFound() throws Exception {
        mockMvc().perform(get("/api/noise/classifications/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }
}
