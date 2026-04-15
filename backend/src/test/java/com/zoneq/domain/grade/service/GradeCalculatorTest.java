package com.zoneq.domain.grade.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GradeCalculatorTest {

    @Test
    void calculate_gradeS_whenLeqBelow40() {
        // leqScore=(60-38)/20*100=100, peakScore=100, trend=50 → total=90 → S
        List<VisitSummary> visits = List.of(new VisitSummary(38.0, 0.0));
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.grade()).isEqualTo("S");
        assertThat(result.totalScore()).isGreaterThanOrEqualTo(80.0);
    }

    @Test
    void calculate_gradeA_whenLeqAround45() {
        // leqScore=75, peakScore=80, trend=50 → total=71.5 → A
        List<VisitSummary> visits = List.of(new VisitSummary(45.0, 4.0));
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.grade()).isEqualTo("A");
    }

    @Test
    void calculate_gradeB_whenLeqAround55_lowPeak() {
        // leqScore=25, peakScore=85, trend=50 → total=48 → B
        List<VisitSummary> visits = List.of(new VisitSummary(55.0, 3.0));
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.grade()).isEqualTo("B");
    }

    @Test
    void calculate_gradeC_whenLeqAbove60() {
        // leqScore=0(clamp), peakScore=40, trend=50 → total=22 → C
        List<VisitSummary> visits = List.of(new VisitSummary(63.0, 12.0));
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.grade()).isEqualTo("C");
    }

    @Test
    void calculate_singleVisit_trendScoreIs50() {
        List<VisitSummary> visits = List.of(new VisitSummary(50.0, 5.0));
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.trendScore()).isEqualTo(50.0);
    }

    @Test
    void calculate_improvingTrend_higherScoreThanDegrading() {
        // improving: old=58 → new=45, delta=+13 → trendScore 높음
        GradeResult improving = GradeCalculator.calculate(List.of(
                new VisitSummary(58.0, 5.0),
                new VisitSummary(45.0, 5.0)
        ));
        // degrading: old=45 → new=58, delta=-13 → trendScore 낮음
        GradeResult degrading = GradeCalculator.calculate(List.of(
                new VisitSummary(45.0, 5.0),
                new VisitSummary(58.0, 5.0)
        ));
        assertThat(improving.totalScore()).isGreaterThan(degrading.totalScore());
    }

    @Test
    void calculate_visitCountReflectsInputSize() {
        List<VisitSummary> visits = List.of(
                new VisitSummary(45.0, 3.0),
                new VisitSummary(43.0, 2.0),
                new VisitSummary(41.0, 1.0)
        );
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.visitCount()).isEqualTo(3);
    }

    @Test
    void calculate_scoresAreClamped_whenLeqVeryLow() {
        // leq=20 → leqScore should be 100 (clamped), not 200
        List<VisitSummary> visits = List.of(new VisitSummary(20.0, 0.0));
        GradeResult result = GradeCalculator.calculate(visits);
        assertThat(result.leqScore()).isEqualTo(100.0);
    }
}
