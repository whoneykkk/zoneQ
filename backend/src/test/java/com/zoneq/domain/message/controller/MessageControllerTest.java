package com.zoneq.domain.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zoneq.domain.auth.dto.SignupRequest;
import com.zoneq.domain.message.dto.MessageReplyRequest;
import com.zoneq.domain.message.dto.MessageSendRequest;
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
class MessageControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private String senderToken;
    private String receiverToken;
    private Long senderSeatId;
    private Long receiverSeatId;
    private Long namedMessageId;
    private Long anonMessageId;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
    }

    @BeforeAll
    void setUp() throws Exception {
        // 발신자 가입
        MvcResult r1 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("발신자", "msg_sender@test.com", "password123!"))))
                .andReturn();
        senderToken = objectMapper.readTree(r1.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        // 수신자 가입
        MvcResult r2 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("수신자", "msg_receiver@test.com", "password123!"))))
                .andReturn();
        receiverToken = objectMapper.readTree(r2.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        // 좌석 생성
        jdbcTemplate.execute(
                "INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('A', 10, 'AVAILABLE', CURRENT_TIMESTAMP)");
        jdbcTemplate.execute(
                "INSERT INTO seats (zone, seat_number, status, created_at) VALUES ('B', 10, 'AVAILABLE', CURRENT_TIMESTAMP)");

        senderSeatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE zone='A' AND seat_number=10", Long.class);
        receiverSeatId = jdbcTemplate.queryForObject(
                "SELECT id FROM seats WHERE zone='B' AND seat_number=10", Long.class);

        Long senderId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email='msg_sender@test.com'", Long.class);
        Long receiverId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email='msg_receiver@test.com'", Long.class);

        // 발신자·수신자 좌석에 착석
        jdbcTemplate.update("UPDATE seats SET status='OCCUPIED', user_id=? WHERE id=?",
                senderId, senderSeatId);
        jdbcTemplate.update("UPDATE seats SET status='OCCUPIED', user_id=? WHERE id=?",
                receiverId, receiverSeatId);

        // 세션 생성
        jdbcTemplate.execute(
                "INSERT INTO sessions (user_id, seat_id, started_at, created_at) VALUES (" +
                "(SELECT id FROM users WHERE email='msg_sender@test.com'), " +
                senderSeatId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.execute(
                "INSERT INTO sessions (user_id, seat_id, started_at, created_at) VALUES (" +
                "(SELECT id FROM users WHERE email='msg_receiver@test.com'), " +
                receiverSeatId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
    }

    @Test
    @Order(1)
    void send_namedMessage_returns200() throws Exception {
        MessageSendRequest req = new MessageSendRequest(receiverSeatId, "안녕하세요", false);

        MvcResult result = mockMvc().perform(post("/api/messages")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").exists())
                .andReturn();

        namedMessageId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/messageId").asLong();
    }

    @Test
    @Order(2)
    void send_anonymousMessage_returns200() throws Exception {
        MessageSendRequest req = new MessageSendRequest(receiverSeatId, "조용히 해주세요", true);

        MvcResult result = mockMvc().perform(post("/api/messages")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").exists())
                .andReturn();

        anonMessageId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/messageId").asLong();
    }

    @Test
    @Order(3)
    void getInbox_returns200_withMessages() throws Exception {
        mockMvc().perform(get("/api/messages/inbox")
                        .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(4)
    void getMessage_namedMessage_canReply_isTrue() throws Exception {
        mockMvc().perform(get("/api/messages/" + namedMessageId)
                        .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canReply").value(true))
                .andExpect(jsonPath("$.data.senderSeat").value("A-10"));
    }

    @Test
    @Order(5)
    void getMessage_anonymousMessage_canReply_isFalse() throws Exception {
        mockMvc().perform(get("/api/messages/" + anonMessageId)
                        .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canReply").value(false))
                .andExpect(jsonPath("$.data.senderSeat").value("익명"));
    }

    @Test
    @Order(6)
    void getMessage_returns403_whenNotReceiver() throws Exception {
        mockMvc().perform(get("/api/messages/" + namedMessageId)
                        .header("Authorization", "Bearer " + senderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void reply_namedMessage_returns200() throws Exception {
        MessageReplyRequest req = new MessageReplyRequest("답장입니다");

        mockMvc().perform(post("/api/messages/" + namedMessageId + "/reply")
                        .header("Authorization", "Bearer " + receiverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").exists());
    }

    @Test
    @Order(8)
    void reply_anonymousMessage_returns403() throws Exception {
        MessageReplyRequest req = new MessageReplyRequest("답장 시도");

        mockMvc().perform(post("/api/messages/" + anonMessageId + "/reply")
                        .header("Authorization", "Bearer " + receiverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void send_returns401_withoutToken() throws Exception {
        MessageSendRequest req = new MessageSendRequest(receiverSeatId, "test", true);

        mockMvc().perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
