package com.zoneq.domain.profile.service;

import com.zoneq.domain.noise.domain.NoiseCategory;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
import com.zoneq.domain.profile.dto.NoiseCategoryRatio;
import com.zoneq.domain.profile.dto.ProfileResponse;
import com.zoneq.domain.profile.dto.ProfileUpdateRequest;
import com.zoneq.domain.session.domain.Session;
import com.zoneq.domain.session.repository.SessionRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final NoiseMeasurementRepository noiseMeasurementRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long visitCount = sessionRepository.countByUserIdAndEndedAtIsNotNull(user.getId());
        NoiseCategoryRatio ratio = buildCategoryRatio(user.getId());
        return ProfileResponse.of(user, visitCount, ratio);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.name() != null) {
            user.updateName(request.name());
        }

        if (request.newPassword() != null) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            user.updatePassword(passwordEncoder.encode(request.newPassword()));
        }

        long visitCount = sessionRepository.countByUserIdAndEndedAtIsNotNull(user.getId());
        NoiseCategoryRatio ratio = buildCategoryRatio(user.getId());
        return ProfileResponse.of(user, visitCount, ratio);
    }

    private NoiseCategoryRatio buildCategoryRatio(Long userId) {
        List<Session> sessions = sessionRepository.findAllByUserIdAndEndedAtIsNotNull(userId);
        if (sessions.isEmpty()) return NoiseCategoryRatio.zero();

        List<Long> sessionIds = sessions.stream().map(Session::getId).toList();
        List<Object[]> counts = noiseMeasurementRepository.countByCategoryInSessions(sessionIds);

        long total = counts.stream().mapToLong(r -> (Long) r[1]).sum();
        if (total == 0) return NoiseCategoryRatio.zero();

        Map<NoiseCategory, Long> countMap = counts.stream()
                .collect(Collectors.toMap(r -> (NoiseCategory) r[0], r -> (Long) r[1]));

        return new NoiseCategoryRatio(
                Math.round(countMap.getOrDefault(NoiseCategory.TALK, 0L) * 1000.0 / total) / 10.0,
                Math.round(countMap.getOrDefault(NoiseCategory.KEYBOARD, 0L) * 1000.0 / total) / 10.0,
                Math.round(countMap.getOrDefault(NoiseCategory.COUGH, 0L) * 1000.0 / total) / 10.0,
                Math.round(countMap.getOrDefault(NoiseCategory.OTHER, 0L) * 1000.0 / total) / 10.0
        );
    }
}
