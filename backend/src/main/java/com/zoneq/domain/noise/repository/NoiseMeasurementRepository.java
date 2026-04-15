package com.zoneq.domain.noise.repository;

import com.zoneq.domain.noise.domain.NoiseMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoiseMeasurementRepository extends JpaRepository<NoiseMeasurement, Long> {
    List<NoiseMeasurement> findBySessionIdAndSeatIdAndMeasuredAtAfter(
            Long sessionId, Long seatId, LocalDateTime after);
    List<NoiseMeasurement> findBySessionIdIn(List<Long> sessionIds);

    @Query("SELECT n FROM NoiseMeasurement n WHERE n.id IN " +
           "(SELECT MAX(n2.id) FROM NoiseMeasurement n2 WHERE n2.seat.id IN :seatIds GROUP BY n2.seat.id)")
    List<NoiseMeasurement> findLatestBySeatIds(@Param("seatIds") List<Long> seatIds);
}
