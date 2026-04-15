package com.zoneq.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NoticeCreateRequest(
        @NotBlank @Schema(description = "공지 제목", example = "이용 안내") String title,
        @NotBlank @Schema(description = "공지 본문") String body,
        @Schema(description = "고정 공지 여부", example = "false") boolean isPinned
) {}
