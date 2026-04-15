package com.zoneq.domain.grade.service;

import java.util.List;

public class GradeCalculator {

    private GradeCalculator() {}

    /**
     * @param visits 오래된 방문부터 최신 방문 순서의 리스트 (최대 5개)
     */
    public static GradeResult calculate(List<VisitSummary> visits) {
        int visitCount = visits.size();

        double avgLeq  = visits.stream().mapToDouble(VisitSummary::avgLeq).average().orElse(0);
        double avgPeak = visits.stream().mapToDouble(VisitSummary::avgPeak).average().orElse(0);

        double leqScore  = clamp((60.0 - avgLeq) / 20.0 * 100.0);
        double peakScore = clamp((1.0 - avgPeak / 20.0) * 100.0);
        double trendScore = computeTrend(visits);

        double totalScore = leqScore * 0.5 + peakScore * 0.3 + trendScore * 0.2;
        String grade = toGrade(totalScore);

        return new GradeResult(grade, round(totalScore), round(leqScore),
                round(peakScore), round(trendScore), round(avgLeq), round(avgPeak), visitCount);
    }

    private static double computeTrend(List<VisitSummary> visits) {
        if (visits.size() < 2) return 50.0;
        double oldest = visits.get(0).avgLeq();
        double newest = visits.get(visits.size() - 1).avgLeq();
        double delta = oldest - newest; // 양수 = 개선, 음수 = 악화
        return clamp(50.0 + delta / 20.0 * 50.0);
    }

    private static String toGrade(double score) {
        if (score >= 80) return "S";
        if (score >= 60) return "A";
        if (score >= 40) return "B";
        return "C";
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
