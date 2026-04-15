package com.zoneq.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;

public record NoticeListResponse(
        @Schema(description = "공지 목록") List<NoticeResponse> notices,
        @Schema(description = "전체 건수") long totalElements,
        @Schema(description = "전체 페이지 수") int totalPages
) {
    public static NoticeListResponse from(Page<NoticeResponse> page) {
        return new NoticeListResponse(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }
}
