package com.zoneq.domain.session.repository;

import com.zoneq.domain.session.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByUserIdAndEndedAtIsNull(Long userId);
    List<Session> findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(Long userId);
}
