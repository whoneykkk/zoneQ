package com.zoneq.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NoticeUpdateRequest(
        @Schema(description = "제목 (변경할 경우만)") String title,
        @Schema(description = "본문 (변경할 경우만)") String body,
        @Schema(description = "고정 여부 (변경할 경우만)") Boolean isPinned
) {}
