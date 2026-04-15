package com.zoneq.domain.grade.service;

import com.zoneq.domain.grade.domain.GradeHistory;
import com.zoneq.domain.grade.dto.GradeDistributionResponse;
import com.zoneq.domain.grade.dto.GradeScoreResponse;
import com.zoneq.domain.grade.repository.GradeHistoryRepository;
import com.zoneq.domain.noise.domain.NoiseMeasurement;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private NoiseMeasurementRepository noiseMeasurementRepository;
    @Mock private GradeHistoryRepository gradeHistoryRepository;

    @InjectMocks
    private GradeService gradeService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create("테스터", "test@test.com", "pw", UserRole.USER);
    }

    // ── recalculate ──────────────────────────────────────────────────

    @Test
    void recalculate_doesNothing_whenNoSessions() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(1L))
                .thenReturn(List.of());

        gradeService.recalculate(1L);

        verifyNoInteractions(noiseMeasurementRepository, gradeHistoryRepository);
    }

    @Test
    void recalculate_doesNothing_whenSessionsHaveNoMeasurements() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(1L))
                .thenReturn(List.of(session));
        when(noiseMeasurementRepository.findBySessionIdIn(List.of(10L)))
                .thenReturn(List.of());

        gradeService.recalculate(1L);

        verifyNoInteractions(gradeHistoryRepository);
    }

    @Test
    void recalculate_updatesGradeAndSavesHistory_whenGradeChanges() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(10L);

        NoiseMeasurement m = mock(NoiseMeasurement.class);
        when(m.getSession()).thenReturn(session);
        when(m.getLeqDb()).thenReturn(38.0);
        when(m.getPeakCount()).thenReturn(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(1L))
                .thenReturn(List.of(session));
        when(noiseMeasurementRepository.findBySessionIdIn(List.of(10L)))
                .thenReturn(List.of(m));
        when(gradeHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        gradeService.recalculate(1L);

        // mockUser.grade가 null이었으므로 변경 발생 → GradeHistory 저장
        verify(gradeHistoryRepository).save(any(GradeHistory.class));
        assertThat(mockUser.getGrade()).isEqualTo("S");
    }

    @Test
    void recalculate_doesNotSaveHistory_whenGradeUnchanged() {
        mockUser.updateGrade("S");

        Session session = mock(Session.class);
        when(session.getId()).thenReturn(10L);

        NoiseMeasurement m = mock(NoiseMeasurement.class);
        when(m.getSession()).thenReturn(session);
        when(m.getLeqDb()).thenReturn(38.0);
        when(m.getPeakCount()).thenReturn(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(1L))
                .thenReturn(List.of(session));
        when(noiseMeasurementRepository.findBySessionIdIn(List.of(10L)))
                .thenReturn(List.of(m));

        gradeService.recalculate(1L);

        verifyNoInteractions(gradeHistoryRepository);
    }

    // ── getMyGrade ───────────────────────────────────────────────────

    @Test
    void getMyGrade_returnsNoData_whenNoSessions() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(any()))
                .thenReturn(List.of());

        GradeScoreResponse response = gradeService.getMyGrade("test@test.com");

        assertThat(response.grade()).isNull();
        assertThat(response.visitCount()).isEqualTo(0);
    }

    @Test
    void getMyGrade_throwsUserNotFound_whenEmailNotExist() {
        when(userRepository.findByEmail("no@no.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.getMyGrade("no@no.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    // ── getDistribution ──────────────────────────────────────────────

    @Test
    void getDistribution_returnsCorrectCounts() {
        when(userRepository.countByGrade()).thenReturn(List.of(
                new Object[]{"S", 3L},
                new Object[]{"A", 5L},
                new Object[]{null, 2L}
        ));

        GradeDistributionResponse response = gradeService.getDistribution();

        assertThat(response.S()).isEqualTo(3L);
        assertThat(response.A()).isEqualTo(5L);
        assertThat(response.B()).isEqualTo(0L);
        assertThat(response.C()).isEqualTo(0L);
        assertThat(response.ungraded()).isEqualTo(2L);
    }
}
