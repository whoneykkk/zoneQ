package com.zoneq.domain.noise.service;

import com.zoneq.domain.noise.domain.CalibrationMap;
import com.zoneq.domain.noise.repository.CalibrationMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoiseAttributionService {

    private final CalibrationMapRepository calibrationMapRepository;

    /**
     * 캘리브레이션 맵 역산으로 소음원 좌석 ID 특정.
     * 추정 원본 dB = receivedLeqDb + attenuationDb, 가장 높은 sourceSeat 반환.
     */
    public Optional<Long> attribute(Long receiverSeatId, double receivedLeqDb) {
        List<CalibrationMap> maps = calibrationMapRepository.findByReceiverSeatId(receiverSeatId);
        if (maps.isEmpty()) {
            return Optional.empty();
        }
        return maps.stream()
                .max(Comparator.comparingDouble(m -> receivedLeqDb + m.getAttenuationDb()))
                .map(m -> m.getSourceSeat().getId());
    }
}
