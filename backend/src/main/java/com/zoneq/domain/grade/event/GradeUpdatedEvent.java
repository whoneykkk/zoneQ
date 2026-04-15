package com.zoneq.domain.grade.event;

public record GradeUpdatedEvent(Long userId, String newGrade) {}
