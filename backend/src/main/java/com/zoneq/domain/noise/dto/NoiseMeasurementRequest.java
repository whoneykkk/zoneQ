package com.zoneq.domain.noise.dto;

import com.zoneq.domain.noise.domain.NoiseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NoiseMeasurementRequest(
        @Schema(description = "좌석 ID", example = "1")
        @NotNull Long seatId,

        @Schema(description = "30초 Leq 값 (dB)", example = "47.3")
        @NotNull Double leqDb,

        @Schema(description = "피크 발생 횟수", example = "2")
        @NotNull Integer peakCount,

        @Schema(description = "측정 지속 시간 (초)", example = "30")
        @NotNull Integer durationSec,

        @Schema(description = "소음 유형 (TALK|KEYBOARD|COUGH|OTHER)", example = "KEYBOARD")
        @NotNull NoiseCategory noiseCategory,

        @Schema(description = "측정 시각", example = "2026-04-15T10:00:00")
        @NotNull LocalDateTime measuredAt
) {}
