package com.zoneq.domain.message.dto;

import com.zoneq.domain.message.domain.Message;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "받은 쪽지 목록 항목")
public record MessageInboxResponse(
        @Schema(description = "쪽지 ID", example = "15") Long id,
        @Schema(description = "발신 좌석. 익명이면 '익명'", example = "B-3") String senderSeat,
        @Schema(description = "쪽지 내용", example = "조용히 해주세요") String body,
        @Schema(description = "읽음 여부", example = "false") boolean isRead,
        @Schema(description = "발송 시각") LocalDateTime sentAt,
        @Schema(description = "답장 가능 여부. 익명=false", example = "true") boolean canReply
) {
    public static MessageInboxResponse from(Message message) {
        String senderSeat = resolveSenderSeat(message);
        return new MessageInboxResponse(
                message.getId(), senderSeat, message.getBody(),
                message.isRead(), message.getSentAt(), !message.isAnonymous()
        );
    }

    private static String resolveSenderSeat(Message message) {
        if (message.isAnonymous()) return "익명";
        if (message.getSeat() == null) return "알 수 없음";
        return message.getSeat().getZone() + "-" + message.getSeat().getSeatNumber();
    }
}
