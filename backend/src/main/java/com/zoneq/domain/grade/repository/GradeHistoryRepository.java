package com.zoneq.domain.grade.repository;

import com.zoneq.domain.grade.domain.GradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeHistoryRepository extends JpaRepository<GradeHistory, Long> {
    List<GradeHistory> findByUserIdOrderByChangedAtDesc(Long userId);
}
