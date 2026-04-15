package com.zoneq.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "쪽지 답장 요청")
public record MessageReplyRequest(
        @Schema(description = "답장 내용", example = "죄송합니다") @NotBlank String body
) {}
