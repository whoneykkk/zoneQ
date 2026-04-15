package com.zoneq.domain.noise.service;

import com.zoneq.domain.noise.domain.NoiseCategory;
import com.zoneq.domain.noise.domain.NoiseMeasurement;
import com.zoneq.domain.noise.dto.CalibrationEntryRequest;
import com.zoneq.domain.noise.event.NoiseWarningEvent;
import com.zoneq.domain.noise.dto.CalibrationRequest;
import com.zoneq.domain.noise.dto.NoiseMeasurementRequest;
import com.zoneq.domain.noise.dto.NoiseClassificationResponse;
import com.zoneq.domain.noise.repository.CalibrationMapRepository;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.repository.SeatRepository;
import com.zoneq.domain.session.domain.Session;
import com.zoneq.domain.session.repository.SessionRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.domain.UserRole;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoiseServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private NoiseMeasurementRepository noiseMeasurementRepository;
    @Mock private CalibrationMapRepository calibrationMapRepository;
    @Mock private NoiseAttributionService noiseAttributionService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NoiseService noiseService;

    private User mockUser;
    private Seat mockSeat;
    private Session mockSession;

    @BeforeEach
    void setUp() {
        mockUser = User.create("테스터", "test@test.com", "encoded", UserRole.USER);
        mockSeat = mock(Seat.class);
        when(mockSeat.getId()).thenReturn(1L);
        mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn(1L);
    }

    // ── saveMeasurement ──────────────────────────────────────────

    @Test
    void saveMeasurement_setsIsHabitual_false_whenLessThanTwoPrevious() {
        NoiseMeasurementRequest req = new NoiseMeasurementRequest(
                1L, 47.3, 2, 30, NoiseCategory.KEYBOARD, LocalDateTime.now());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByUserIdAndEndedAtIsNull(any())).thenReturn(Optional.of(mockSession));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(mockSeat));
        // 이전 측정 1건 → 이번 포함 2건 → 습관성 아님
        when(noiseMeasurementRepository.findBySessionIdAndSeatIdAndMeasuredAtAfter(
                any(), any(), any())).thenReturn(List.of(mock(NoiseMeasurement.class)));
        when(noiseMeasurementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(noiseAttributionService.attribute(any(), anyDouble())).thenReturn(Optional.empty());

        noiseService.saveMeasurement("test@test.com", req);

        ArgumentCaptor<NoiseMeasurement> captor = ArgumentCaptor.forClass(NoiseMeasurement.class);
        verify(noiseMeasurementRepository).save(captor.capture());
        assertThat(captor.getValue().getIsHabitual()).isFalse();
        verify(eventPublisher, never()).publishEvent(any(NoiseWarningEvent.class));
    }

    @Test
    void saveMeasurement_setsIsHabitual_true_whenTwoOrMorePrevious() {
        NoiseMeasurementRequest req = new NoiseMeasurementRequest(
                1L, 55.0, 5, 30, NoiseCategory.TALK, LocalDateTime.now());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByUserIdAndEndedAtIsNull(any())).thenReturn(Optional.of(mockSession));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(mockSeat));
        // 이전 측정 2건 → 이번 포함 3건 → 습관성
        when(noiseMeasurementRepository.findBySessionIdAndSeatIdAndMeasuredAtAfter(
                any(), any(), any())).thenReturn(List.of(
                mock(NoiseMeasurement.class), mock(NoiseMeasurement.class)));
        when(noiseMeasurementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(noiseAttributionService.attribute(any(), anyDouble())).thenReturn(Optional.empty());

        noiseService.saveMeasurement("test@test.com", req);

        ArgumentCaptor<NoiseMeasurement> captor = ArgumentCaptor.forClass(NoiseMeasurement.class);
        verify(noiseMeasurementRepository).save(captor.capture());
        assertThat(captor.getValue().getIsHabitual()).isTrue();
        verify(eventPublisher, never()).publishEvent(any(NoiseWarningEvent.class));
    }

    @Test
    void saveMeasurement_publishesNoiseWarningEvent_whenLeqDbAtOrAbove60() {
        NoiseMeasurementRequest req = new NoiseMeasurementRequest(
                1L, 60.0, 3, 30, NoiseCategory.TALK, LocalDateTime.now());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByUserIdAndEndedAtIsNull(any())).thenReturn(Optional.of(mockSession));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(mockSeat));
        when(noiseMeasurementRepository.findBySessionIdAndSeatIdAndMeasuredAtAfter(
                any(), any(), any())).thenReturn(List.of());
        when(noiseMeasurementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(noiseAttributionService.attribute(any(), anyDouble())).thenReturn(Optional.empty());

        noiseService.saveMeasurement("test@test.com", req);

        verify(eventPublisher).publishEvent(any(NoiseWarningEvent.class));
    }

    @Test
    void saveMeasurement_throwsSessionNotFound_whenNoActiveSession() {
        NoiseMeasurementRequest req = new NoiseMeasurementRequest(
                1L, 47.3, 2, 30, NoiseCategory.OTHER, LocalDateTime.now());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByUserIdAndEndedAtIsNull(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noiseService.saveMeasurement("test@test.com", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SESSION_NOT_FOUND.getMessage());
    }

    // ── saveCalibration ──────────────────────────────────────────

    @Test
    void saveCalibration_deletesAllAndSavesNew() {
        Seat src = mock(Seat.class);
        Seat rcv = mock(Seat.class);
        CalibrationRequest req = new CalibrationRequest(
                List.of(new CalibrationEntryRequest(1L, 2L, 5.0)));

        when(seatRepository.findById(1L)).thenReturn(Optional.of(src));
        when(seatRepository.findById(2L)).thenReturn(Optional.of(rcv));
        when(calibrationMapRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        int count = noiseService.saveCalibration(req);

        verify(calibrationMapRepository).deleteAllInBatch();
        verify(calibrationMapRepository).saveAll(anyList());
        assertThat(count).isEqualTo(1);
    }

    // ── getClassification ────────────────────────────────────────

    @Test
    void getClassification_returnsResponse() {
        NoiseMeasurement measurement = NoiseMeasurement.of(
                mockSession, mockSeat, 47.3, 2, NoiseCategory.KEYBOARD, false, LocalDateTime.now());
        when(noiseMeasurementRepository.findById(1L)).thenReturn(Optional.of(measurement));

        NoiseClassificationResponse response = noiseService.getClassification(1L);

        assertThat(response.noiseCategory()).isEqualTo(NoiseCategory.KEYBOARD);
        assertThat(response.isHabitual()).isFalse();
        assertThat(response.leqDb()).isEqualTo(47.3);
    }

    @Test
    void getClassification_throwsNotFound_whenIdNotExist() {
        when(noiseMeasurementRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noiseService.getClassification(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOISE_MEASUREMENT_NOT_FOUND.getMessage());
    }
}
