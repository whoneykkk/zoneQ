package com.zoneq.domain.grade.dto;

import com.zoneq.domain.grade.service.GradeResult;
import com.zoneq.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "특정 이용자 등급 + 점수 구성 (ADMIN)")
public record GradeDetailResponse(
        @Schema(description = "이용자 ID", example = "1") Long userId,
        @Schema(description = "이용자 이름", example = "홍길동") String userName,
        @Schema(description = "소음 등급", example = "A") String grade,
        @Schema(description = "종합 점수", example = "72.5") Double totalScore,
        @Schema(description = "Leq 점수", example = "78.0") Double leqScore,
        @Schema(description = "피크 빈도 점수", example = "65.0") Double peakScore,
        @Schema(description = "개선 추이 점수", example = "60.0") Double trendScore,
        @Schema(description = "평균 Leq (dB)", example = "44.4") Double avgLeqDb,
        @Schema(description = "평균 피크 횟수", example = "7.0") Double avgPeakCount,
        @Schema(description = "집계된 방문 횟수", example = "5") Integer visitCount
) {
    public static GradeDetailResponse of(User user, GradeResult result) {
        return new GradeDetailResponse(
                user.getId(), user.getName(),
                result.grade(), result.totalScore(), result.leqScore(),
                result.peakScore(), result.trendScore(),
                result.avgLeqDb(), result.avgPeakCount(), result.visitCount()
        );
    }

    public static GradeDetailResponse noData(User user) {
        return new GradeDetailResponse(user.getId(), user.getName(),
                null, null, null, null, null, null, null, 0);
    }
}
