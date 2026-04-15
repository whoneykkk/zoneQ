package com.zoneq.domain.noise.service;

import com.zoneq.domain.noise.domain.CalibrationMap;
import com.zoneq.domain.noise.domain.NoiseMeasurement;
import com.zoneq.domain.noise.dto.*;
import com.zoneq.domain.noise.event.NoiseWarningEvent;
import com.zoneq.domain.noise.repository.CalibrationMapRepository;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.repository.SeatRepository;
import com.zoneq.domain.session.domain.Session;
import com.zoneq.domain.session.repository.SessionRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoiseService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final NoiseMeasurementRepository noiseMeasurementRepository;
    private final CalibrationMapRepository calibrationMapRepository;
    private final NoiseAttributionService noiseAttributionService;
    private final ApplicationEventPublisher eventPublisher;

    @Async("noiseAsyncExecutor")
    @Transactional
    public void saveMeasurement(String email, NoiseMeasurementRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Session session = sessionRepository.findByUserIdAndEndedAtIsNull(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        boolean habitual = isHabitual(session.getId(), seat.getId());

        NoiseMeasurement measurement = NoiseMeasurement.of(
                session, seat,
                request.leqDb(), request.peakCount(),
                request.noiseCategory(), habitual,
                request.measuredAt()
        );
        noiseMeasurementRepository.save(measurement);

        if (measurement.getLeqDb() >= 60) {
            eventPublisher.publishEvent(new NoiseWarningEvent(user.getId(), measurement.getLeqDb()));
        }

        noiseAttributionService.attribute(seat.getId(), request.leqDb())
                .ifPresent(sourceSeatId ->
                        log.info("소음 귀속: receiverSeat={}, sourceSeat={}, leqDb={}",
                                seat.getId(), sourceSeatId, request.leqDb()));
    }

    @Transactional
    public int saveCalibration(CalibrationRequest request) {
        calibrationMapRepository.deleteAllInBatch();

        List<CalibrationMap> maps = request.entries().stream()
                .map(entry -> {
                    Seat sourceSeat = seatRepository.findById(entry.sourceSeatId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
                    Seat receiverSeat = seatRepository.findById(entry.receiverSeatId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
                    return CalibrationMap.of(sourceSeat, receiverSeat, entry.attenuationDb());
                })
                .toList();

        calibrationMapRepository.saveAll(maps);
        return maps.size();
    }

    @Transactional(readOnly = true)
    public NoiseClassificationResponse getClassification(Long id) {
        NoiseMeasurement measurement = noiseMeasurementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOISE_MEASUREMENT_NOT_FOUND));
        return NoiseClassificationResponse.from(measurement);
    }

    private boolean isHabitual(Long sessionId, Long seatId) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<NoiseMeasurement> recent = noiseMeasurementRepository
                .findBySessionIdAndSeatIdAndMeasuredAtAfter(sessionId, seatId, tenMinutesAgo);
        // 이전 2건 이상 존재 → 이번 포함 3회 이상 → 습관성
        return recent.size() >= 2;
    }
}
