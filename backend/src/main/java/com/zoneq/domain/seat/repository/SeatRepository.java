package com.zoneq.domain.seat.repository;

import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByZone(String zone);
    List<Seat> findByZoneAndStatus(String zone, SeatStatus status);
    List<Seat> findByStatus(SeatStatus status);
    Optional<Seat> findByUserId(Long userId);
}
