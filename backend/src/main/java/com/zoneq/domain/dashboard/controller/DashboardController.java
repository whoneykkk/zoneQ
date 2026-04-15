package com.zoneq.domain.dashboard.controller;

import com.zoneq.domain.dashboard.dto.DashboardStatsResponse;
import com.zoneq.domain.dashboard.service.DashboardService;
import com.zoneq.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Dashboard", description = "관리자 대시보드 API (ADMIN 전용)")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "실시간 소음 스트리밍 (SSE)",
               description = "착석 좌석별 최신 leqDb를 5초 간격으로 push합니다. ADMIN 전용.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRealtime() {
        return dashboardService.subscribe();
    }

    @Operation(summary = "대시보드 통계 조회",
               description = "총 좌석·재실 인원·평균 dB·오늘 경고 수·등급 분포를 반환합니다. ADMIN 전용.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> getStats() {
        return ApiResponse.ok(dashboardService.getStats());
    }
}
