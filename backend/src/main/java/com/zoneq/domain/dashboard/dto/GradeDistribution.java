package com.zoneq.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GradeDistribution(
        @Schema(description = "S등급 수", example = "2") int s,
        @Schema(description = "A등급 수", example = "5") int a,
        @Schema(description = "B등급 수", example = "8") int b,
        @Schema(description = "C등급 수", example = "3") int c,
        @Schema(description = "등급 미부여 수", example = "10") int ungraded
) {}
