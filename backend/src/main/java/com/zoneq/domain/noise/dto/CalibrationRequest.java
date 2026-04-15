package com.zoneq.domain.noise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CalibrationRequest(
        @Schema(description = "감쇠 맵 목록 (기존 맵 전체 교체)")
        @NotEmpty @Valid List<CalibrationEntryRequest> entries
) {}
