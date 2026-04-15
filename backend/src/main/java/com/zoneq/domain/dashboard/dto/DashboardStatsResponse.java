package com.zoneq.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DashboardStatsResponse(
        @Schema(description = "전체 좌석 수", example = "40") int totalSeats,
        @Schema(description = "현재 착석 중인 좌석 수", example = "12") int occupiedSeats,
        @Schema(description = "착석 좌석 평균 leqDb (착석 없으면 null)", example = "48.5") Double avgLeqDb,
        @Schema(description = "오늘 발생한 소음 경고 수", example = "3") int warningCount,
        @Schema(description = "전체 이용자 등급 분포") GradeDistribution gradeDistribution
) {}
