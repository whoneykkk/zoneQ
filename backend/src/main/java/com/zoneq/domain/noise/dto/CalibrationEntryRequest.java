package com.zoneq.domain.noise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CalibrationEntryRequest(
        @Schema(description = "소음 발생 좌석 ID", example = "1")
        @NotNull Long sourceSeatId,

        @Schema(description = "수신 좌석 ID", example = "2")
        @NotNull Long receiverSeatId,

        @Schema(description = "감쇠값 (dB)", example = "5.2")
        @NotNull Double attenuationDb
) {}
