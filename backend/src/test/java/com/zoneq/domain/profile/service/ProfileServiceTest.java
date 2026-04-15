package com.zoneq.domain.profile.service;

import com.zoneq.domain.noise.domain.NoiseCategory;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
import com.zoneq.domain.profile.dto.ProfileResponse;
import com.zoneq.domain.profile.dto.ProfileUpdateRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private NoiseMeasurementRepository noiseMeasurementRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ProfileService profileService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create("테스터", "test@test.com", "encodedPw", UserRole.USER);
    }

    // ── getProfile ──────────────────────────────────────────────────

    @Test
    void getProfile_returnsZeroVisitAndRatios_whenNoSessions() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.countByUserIdAndEndedAtIsNotNull(any())).thenReturn(0L);
        when(sessionRepository.findAllByUserIdAndEndedAtIsNotNull(any())).thenReturn(List.of());

        ProfileResponse response = profileService.getProfile("test@test.com");

        assertThat(response.name()).isEqualTo("테스터");
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.grade()).isNull();
        assertThat(response.visitCount()).isEqualTo(0);
        assertThat(response.noiseCategoryRatio().talk()).isEqualTo(0.0);
        assertThat(response.noiseCategoryRatio().keyboard()).isEqualTo(0.0);
        assertThat(response.noiseCategoryRatio().cough()).isEqualTo(0.0);
        assertThat(response.noiseCategoryRatio().other()).isEqualTo(0.0);
    }

    @Test
    void getProfile_returnsCorrectRatios_whenMeasurementsExist() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(1L);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.countByUserIdAndEndedAtIsNotNull(any())).thenReturn(1L);
        when(sessionRepository.findAllByUserIdAndEndedAtIsNotNull(any())).thenReturn(List.of(session));
        when(noiseMeasurementRepository.countByCategoryInSessions(List.of(1L))).thenReturn(List.of(
                new Object[]{NoiseCategory.TALK, 3L},
                new Object[]{NoiseCategory.OTHER, 7L}
        ));

        ProfileResponse response = profileService.getProfile("test@test.com");

        assertThat(response.visitCount()).isEqualTo(1);
        assertThat(response.noiseCategoryRatio().talk()).isEqualTo(30.0);
        assertThat(response.noiseCategoryRatio().other()).isEqualTo(70.0);
        assertThat(response.noiseCategoryRatio().keyboard()).isEqualTo(0.0);
        assertThat(response.noiseCategoryRatio().cough()).isEqualTo(0.0);
    }

    @Test
    void getProfile_throwsUserNotFound_whenEmailNotExist() {
        when(userRepository.findByEmail("no@no.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile("no@no.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    // ── updateProfile ───────────────────────────────────────────────

    @Test
    void updateProfile_updatesName_whenNameProvided() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.countByUserIdAndEndedAtIsNotNull(any())).thenReturn(0L);
        when(sessionRepository.findAllByUserIdAndEndedAtIsNotNull(any())).thenReturn(List.of());

        ProfileResponse response = profileService.updateProfile(
                "test@test.com", new ProfileUpdateRequest("새이름", null, null));

        assertThat(response.name()).isEqualTo("새이름");
        assertThat(mockUser.getName()).isEqualTo("새이름");
    }

    @Test
    void updateProfile_updatesPassword_whenCurrentPasswordMatches() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPw", "encodedPw")).thenReturn(true);
        when(passwordEncoder.encode("newPw")).thenReturn("newEncodedPw");
        when(sessionRepository.countByUserIdAndEndedAtIsNotNull(any())).thenReturn(0L);
        when(sessionRepository.findAllByUserIdAndEndedAtIsNotNull(any())).thenReturn(List.of());

        profileService.updateProfile(
                "test@test.com", new ProfileUpdateRequest(null, "oldPw", "newPw"));

        assertThat(mockUser.getPassword()).isEqualTo("newEncodedPw");
    }

    @Test
    void updateProfile_throwsInvalidInput_whenCurrentPasswordMissing() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> profileService.updateProfile(
                "test@test.com", new ProfileUpdateRequest(null, null, "newPw")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT.getMessage());
    }

    @Test
    void updateProfile_throwsInvalidInput_whenCurrentPasswordWrong() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPw", "encodedPw")).thenReturn(false);

        assertThatThrownBy(() -> profileService.updateProfile(
                "test@test.com", new ProfileUpdateRequest(null, "wrongPw", "newPw")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT.getMessage());
    }

    @Test
    void updateProfile_returnsCurrentProfile_whenBothFieldsNull() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(sessionRepository.countByUserIdAndEndedAtIsNotNull(any())).thenReturn(0L);
        when(sessionRepository.findAllByUserIdAndEndedAtIsNotNull(any())).thenReturn(List.of());

        ProfileResponse response = profileService.updateProfile(
                "test@test.com", new ProfileUpdateRequest(null, null, null));

        assertThat(response.name()).isEqualTo("테스터");
    }
}
