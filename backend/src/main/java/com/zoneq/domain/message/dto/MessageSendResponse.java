package com.zoneq.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쪽지 발송/답장 응답")
public record MessageSendResponse(
        @Schema(description = "생성된 쪽지 ID", example = "15") Long messageId
) {
    public static MessageSendResponse of(Long id) {
        return new MessageSendResponse(id);
    }
}
