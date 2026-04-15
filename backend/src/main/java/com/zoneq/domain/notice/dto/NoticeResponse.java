package com.zoneq.domain.notice.dto;

import com.zoneq.domain.notice.domain.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NoticeResponse(
        @Schema(description = "공지 ID") Long id,
        @Schema(description = "제목") String title,
        @Schema(description = "본문") String body,
        @Schema(description = "고정 공지 여부") boolean isPinned,
        @Schema(description = "작성 시각") LocalDateTime createdAt,
        @Schema(description = "작성자 이름") String adminName
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getBody(),
                notice.isPinned(),
                notice.getCreatedAt(),
                notice.getAdmin().getName()
        );
    }
}
