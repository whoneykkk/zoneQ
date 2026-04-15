package com.zoneq.domain.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zoneq.domain.auth.dto.SignupRequest;
import com.zoneq.domain.notice.dto.NoticeCreateRequest;
import com.zoneq.domain.notice.dto.NoticeUpdateRequest;
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
class NoticeControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private String userToken;
    private String adminToken;
    private Long noticeId;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
    }

    @BeforeAll
    void setUp() throws Exception {
        MvcResult r1 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("공지유저", "notice_user@test.com", "password123!"))))
                .andReturn();
        userToken = objectMapper.readTree(r1.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        MvcResult r2 = mockMvc().perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("공지관리자", "notice_admin@test.com", "password123!"))))
                .andReturn();
        adminToken = objectMapper.readTree(r2.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
        jdbcTemplate.execute("UPDATE users SET role = 'ADMIN' WHERE email = 'notice_admin@test.com'");
    }

    @Test
    @Order(1)
    void createNotice_returns201_withAdminToken() throws Exception {
        NoticeCreateRequest req = new NoticeCreateRequest("첫 공지", "공지 본문입니다.", true);
        MvcResult result = mockMvc().perform(post("/api/notices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("첫 공지"))
                .andExpect(jsonPath("$.data.isPinned").value(true))
                .andReturn();
        noticeId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();
    }

    @Test
    @Order(2)
    void createNotice_returns403_withUserToken() throws Exception {
        NoticeCreateRequest req = new NoticeCreateRequest("제목", "본문", false);
        mockMvc().perform(post("/api/notices")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void getList_returns200_withUserToken() throws Exception {
        mockMvc().perform(get("/api/notices")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notices").isArray());
    }

    @Test
    @Order(4)
    void getList_pinnedFilter_returnsOnlyPinned() throws Exception {
        mockMvc().perform(get("/api/notices?pinned=true")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notices[0].isPinned").value(true));
    }

    @Test
    @Order(5)
    void getDetail_returns200() throws Exception {
        mockMvc().perform(get("/api/notices/" + noticeId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("첫 공지"));
    }

    @Test
    @Order(6)
    void getDetail_returns404_whenNotFound() throws Exception {
        mockMvc().perform(get("/api/notices/99999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    void updateNotice_returns200_withAdminToken() throws Exception {
        NoticeUpdateRequest req = new NoticeUpdateRequest("수정된 제목", null, null);
        mockMvc().perform(patch("/api/notices/" + noticeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.body").value("공지 본문입니다."));
    }

    @Test
    @Order(8)
    void updateNotice_returns403_withUserToken() throws Exception {
        mockMvc().perform(patch("/api/notices/" + noticeId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new NoticeUpdateRequest("시도", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void deleteNotice_returns204_withAdminToken() throws Exception {
        mockMvc().perform(delete("/api/notices/" + noticeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(10)
    void deleteNotice_returns404_afterDeletion() throws Exception {
        mockMvc().perform(get("/api/notices/" + noticeId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }
}
