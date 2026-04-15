package com.zoneq.domain.dashboard.service;

import com.zoneq.domain.dashboard.dto.DashboardStatsResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private SeatRepository seatRepository;
    @Mock private NoiseMeasurementRepository noiseMeasurementRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private SseEmitterRegistry registry;

    @InjectMocks
    private DashboardService dashboardService;

    private Seat occupiedSeat;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create("테스터", "user@test.com", "pw", UserRole.USER);

        occupiedSeat = spy(Seat.of("A", 3));
        doReturn(1L).when(occupiedSeat).getId();
    }

    @Test
    void getRealtimeData_returnsOccupiedSeatsWithLeqDb() {
        doReturn(mockUser).when(occupiedSeat).getUser();

        NoiseMeasurement measurement = mock(NoiseMeasurement.class);
        when(measurement.getSeat()).thenReturn(occupiedSeat);
        when(measurement.getLeqDb()).thenReturn(52.3);

        when(seatRepository.findByStatus(SeatStatus.OCCUPIED)).thenReturn(List.of(occupiedSeat));
        when(noiseMeasurementRepository.findLatestBySeatIds(List.of(1L))).thenReturn(List.of(measurement));

        List<RealtimeSeatData> result = dashboardService.getRealtimeData();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).zone()).isEqualTo("A");
        assertThat(result.get(0).seatNumber()).isEqualTo(3);
        assertThat(result.get(0).leqDb()).isEqualTo(52.3);
    }

    @Test
    void getRealtimeData_returnsNullLeqDb_whenNoMeasurement() {
        doReturn(mockUser).when(occupiedSeat).getUser();

        when(seatRepository.findByStatus(SeatStatus.OCCUPIED)).thenReturn(List.of(occupiedSeat));
        when(noiseMeasurementRepository.findLatestBySeatIds(List.of(1L))).thenReturn(List.of());

        List<RealtimeSeatData> result = dashboardService.getRealtimeData();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).leqDb()).isNull();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getRealtimeData_returnsEmptyList_whenNoOccupiedSeats() {
        when(seatRepository.findByStatus(SeatStatus.OCCUPIED)).thenReturn(List.of());

        List<RealtimeSeatData> result = dashboardService.getRealtimeData();

        assertThat(result).isEmpty();
        verifyNoInteractions(noiseMeasurementRepository);
    }

    @Test
    void getStats_aggregatesCorrectly() {
        doReturn(SeatStatus.OCCUPIED).when(occupiedSeat).getStatus();

        Seat availableSeat = spy(Seat.of("B", 1));
        doReturn(SeatStatus.AVAILABLE).when(availableSeat).getStatus();

        User adminUser = User.create("관리자", "admin@test.com", "pw", UserRole.ADMIN);
        User gradedUser = spy(User.create("유저2", "u2@test.com", "pw", UserRole.USER));
        doReturn("A").when(gradedUser).getGrade();

        when(seatRepository.findAll()).thenReturn(List.of(occupiedSeat, availableSeat));
        when(noiseMeasurementRepository.findLatestBySeatIds(anyList())).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(mockUser, gradedUser, adminUser));
        when(notificationRepository.countByTypeAndCreatedAtAfter(
                eq(NotificationType.NOISE_WARNING), any(LocalDateTime.class))).thenReturn(3L);

        DashboardStatsResponse stats = dashboardService.getStats();

        assertThat(stats.totalSeats()).isEqualTo(2);
        assertThat(stats.occupiedSeats()).isEqualTo(1);
        assertThat(stats.avgLeqDb()).isNull();
        assertThat(stats.warningCount()).isEqualTo(3);
        assertThat(stats.gradeDistribution().a()).isEqualTo(1);
        assertThat(stats.gradeDistribution().ungraded()).isEqualTo(1);
        assertThat(stats.gradeDistribution().s() + stats.gradeDistribution().b() +
                   stats.gradeDistribution().c()).isEqualTo(0);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void broadcastRealtime_delegatesToRegistry() {
        when(seatRepository.findByStatus(SeatStatus.OCCUPIED)).thenReturn(List.of());

        dashboardService.broadcastRealtime();

        verify(registry).broadcast(anyList());
    }
}
