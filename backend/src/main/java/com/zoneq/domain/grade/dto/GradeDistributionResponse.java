package com.zoneq.domain.grade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "전체 등급 분포 통계")
public record GradeDistributionResponse(
        @Schema(description = "S등급 이용자 수", example = "12") long S,
        @Schema(description = "A등급 이용자 수", example = "34") long A,
        @Schema(description = "B등급 이용자 수", example = "28") long B,
        @Schema(description = "C등급 이용자 수", example = "9") long C,
        @Schema(description = "등급 미부여 이용자 수", example = "5") long ungraded
) {}
