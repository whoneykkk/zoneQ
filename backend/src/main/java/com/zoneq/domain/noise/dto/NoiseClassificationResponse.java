package com.zoneq.domain.noise.dto;

import com.zoneq.domain.noise.domain.NoiseCategory;
import com.zoneq.domain.noise.domain.NoiseMeasurement;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NoiseClassificationResponse(
        @Schema(description = "측정 ID") Long id,
        @Schema(description = "소음 유형") NoiseCategory noiseCategory,
        @Schema(description = "습관적 소음 여부") Boolean isHabitual,
        @Schema(description = "Leq 값 (dB)") Double leqDb,
        @Schema(description = "피크 횟수") Integer peakCount,
        @Schema(description = "측정 시각") LocalDateTime measuredAt
) {
    public static NoiseClassificationResponse from(NoiseMeasurement m) {
        return new NoiseClassificationResponse(
                m.getId(),
                m.getNoiseCategory(),
                m.getIsHabitual(),
                m.getLeqDb(),
                m.getPeakCount(),
                m.getMeasuredAt()
        );
    }
}
