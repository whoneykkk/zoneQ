package com.zoneq.domain.grade.dto;

import com.zoneq.domain.grade.service.GradeResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 등급 + 점수 구성")
public record GradeScoreResponse(
        @Schema(description = "소음 등급", example = "A") String grade,
        @Schema(description = "종합 점수 (0~100)", example = "72.5") Double totalScore,
        @Schema(description = "Leq 점수 (50% 반영)", example = "78.0") Double leqScore,
        @Schema(description = "피크 빈도 점수 (30% 반영)", example = "65.0") Double peakScore,
        @Schema(description = "개선 추이 점수 (20% 반영)", example = "60.0") Double trendScore,
        @Schema(description = "평균 Leq (dB)", example = "44.4") Double avgLeqDb,
        @Schema(description = "평균 피크 횟수", example = "7.0") Double avgPeakCount,
        @Schema(description = "집계된 방문 횟수", example = "5") Integer visitCount
) {
    public static GradeScoreResponse from(GradeResult result) {
        return new GradeScoreResponse(
                result.grade(), result.totalScore(), result.leqScore(),
                result.peakScore(), result.trendScore(),
                result.avgLeqDb(), result.avgPeakCount(), result.visitCount()
        );
    }

    public static GradeScoreResponse noData() {
        return new GradeScoreResponse(null, null, null, null, null, null, null, 0);
    }
}
