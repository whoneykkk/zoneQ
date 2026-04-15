package com.zoneq.domain.noise.repository;

import com.zoneq.domain.noise.domain.NoiseMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NoiseMeasurementRepository extends JpaRepository<NoiseMeasurement, Long> {
    List<NoiseMeasurement> findBySessionIdAndSeatIdAndMeasuredAtAfter(
            Long sessionId, Long seatId, LocalDateTime after);
    List<NoiseMeasurement> findBySessionIdIn(List<Long> sessionIds);
}
