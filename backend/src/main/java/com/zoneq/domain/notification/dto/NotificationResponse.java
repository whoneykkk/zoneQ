package com.zoneq.domain.notification.dto;

import com.zoneq.domain.notification.domain.Notification;
import com.zoneq.domain.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(description = "알림 ID") Long id,
        @Schema(description = "알림 유형") NotificationType type,
        @Schema(description = "알림 내용") String body,
        @Schema(description = "읽음 여부") boolean isRead,
        @Schema(description = "생성 시각") LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getBody(), n.isRead(), n.getCreatedAt()
        );
    }
}
