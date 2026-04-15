package com.zoneq.domain.noise.repository;

import com.zoneq.domain.noise.domain.CalibrationMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalibrationMapRepository extends JpaRepository<CalibrationMap, Long> {
    List<CalibrationMap> findByReceiverSeatId(Long receiverSeatId);
}
