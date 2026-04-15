package com.zoneq.domain.grade.dto;

import com.zoneq.domain.grade.domain.GradeHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "등급 변화 이력 항목")
public record GradeHistoryResponse(
        @Schema(description = "변경된 등급", example = "A") String grade,
        @Schema(description = "변경 시각", example = "2026-04-10T14:30:00") LocalDateTime changedAt
) {
    public static GradeHistoryResponse from(GradeHistory history) {
        return new GradeHistoryResponse(history.getGrade(), history.getChangedAt());
    }
}
