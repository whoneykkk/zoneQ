package com.zoneq.domain.grade.service;

/**
 * 방문 1회의 대표값 (세션 내 측정값 평균).
 * 리스트는 오래된 방문부터 최신 방문 순서로 전달한다.
 */
public record VisitSummary(double avgLeq, double avgPeak) {}
