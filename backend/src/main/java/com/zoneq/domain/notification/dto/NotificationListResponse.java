package com.zoneq.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record NotificationListResponse(
        @Schema(description = "알림 목록") List<NotificationResponse> notifications,
        @Schema(description = "미읽음 수") int unreadCount
) {}
