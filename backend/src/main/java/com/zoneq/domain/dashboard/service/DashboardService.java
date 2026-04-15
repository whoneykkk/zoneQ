package com.zoneq.domain.dashboard.service;

import com.zoneq.domain.dashboard.dto.DashboardStatsResponse;
import com.zoneq.domain.dashboard.dto.GradeDistribution;
import com.zoneq.domain.dashboard.dto.RealtimeSeatData;
import com.zoneq.domain.noise.domain.NoiseMeasurement;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
import com.zoneq.domain.notification.domain.NotificationType;
import com.zoneq.domain.notification.repository.NotificationRepository;
import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.domain.SeatStatus;
import com.zoneq.domain.seat.repository.SeatRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.domain.UserRole;
import com.zoneq.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SeatRepository seatRepository;
    private final NoiseMeasurementRepository noiseMeasurementRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SseEmitterRegistry registry;

    @Transactional(readOnly = true)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        String id = UUID.randomUUID().toString();
        registry.register(id, emitter);

        emitter.onCompletion(() -> registry.remove(id));
        emitter.onError(e -> registry.remove(id));
        emitter.onTimeout(() -> registry.remove(id));

        try {
            emitter.send(SseEmitter.event().data(getRealtimeData()));
        } catch (IOException e) {
            registry.remove(id);
        }

        return emitter;
    }

    @Transactional(readOnly = true)
    public List<RealtimeSeatData> getRealtimeData() {
        List<Seat> occupiedSeats = seatRepository.findByStatus(SeatStatus.OCCUPIED);
        if (occupiedSeats.isEmpty()) return List.of();

        List<Long> seatIds = occupiedSeats.stream().map(Seat::getId).toList();
        Map<Long, Double> latestLeqBySeatId = noiseMeasurementRepository.findLatestBySeatIds(seatIds)
                .stream()
                .collect(Collectors.toMap(
                        n -> n.getSeat().getId(),
                        NoiseMeasurement::getLeqDb
                ));

        return occupiedSeats.stream()
                .map(seat -> new RealtimeSeatData(
                        seat.getId(),
                        seat.getZone(),
                        seat.getSeatNumber(),
                        latestLeqBySeatId.get(seat.getId()),
                        seat.getUser() != null ? seat.getUser().getId() : null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        List<Seat> allSeats = seatRepository.findAll();
        List<Seat> occupiedSeats = allSeats.stream()
                .filter(s -> s.getStatus() == SeatStatus.OCCUPIED)
                .toList();

        int totalSeats = allSeats.size();
        int occupiedCount = occupiedSeats.size();

        Double avgLeqDb = null;
        if (!occupiedSeats.isEmpty()) {
            List<Long> seatIds = occupiedSeats.stream().map(Seat::getId).toList();
            List<NoiseMeasurement> latest = noiseMeasurementRepository.findLatestBySeatIds(seatIds);
            if (!latest.isEmpty()) {
                avgLeqDb = latest.stream()
                        .mapToDouble(NoiseMeasurement::getLeqDb)
                        .average()
                        .orElse(0.0);
            }
        }

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        int warningCount = (int) notificationRepository.countByTypeAndCreatedAtAfter(
                NotificationType.NOISE_WARNING, startOfToday);

        List<User> userRoleUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.USER)
                .toList();

        Map<String, Long> gradeCounts = userRoleUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getGrade() != null ? u.getGrade() : "UNGRADED",
                        Collectors.counting()
                ));

        GradeDistribution dist = new GradeDistribution(
                gradeCounts.getOrDefault("S", 0L).intValue(),
                gradeCounts.getOrDefault("A", 0L).intValue(),
                gradeCounts.getOrDefault("B", 0L).intValue(),
                gradeCounts.getOrDefault("C", 0L).intValue(),
                gradeCounts.getOrDefault("UNGRADED", 0L).intValue()
        );

        return new DashboardStatsResponse(totalSeats, occupiedCount, avgLeqDb, warningCount, dist);
    }

    @Transactional(readOnly = true)
    public void broadcastRealtime() {
        registry.broadcast(getRealtimeData());
    }
}
