package com.zoneq.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "쪽지 발송 요청")
public record MessageSendRequest(
        @Schema(description = "수신 좌석 ID", example = "3") @NotNull Long receiverSeatId,
        @Schema(description = "쪽지 내용", example = "조용히 해주세요") @NotBlank String body,
        @Schema(description = "익명 여부", example = "true") @NotNull Boolean isAnonymous
) {}
