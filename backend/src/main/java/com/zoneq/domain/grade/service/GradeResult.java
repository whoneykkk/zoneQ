package com.zoneq.domain.grade.service;

public record GradeResult(
        String grade,
        double totalScore,
        double leqScore,
        double peakScore,
        double trendScore,
        double avgLeqDb,
        double avgPeakCount,
        int visitCount
) {}
