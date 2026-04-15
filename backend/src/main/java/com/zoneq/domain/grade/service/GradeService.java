package com.zoneq.domain.grade.service;

import com.zoneq.domain.grade.domain.GradeHistory;
import com.zoneq.domain.grade.dto.*;
import com.zoneq.domain.grade.event.GradeUpdatedEvent;
import com.zoneq.domain.grade.repository.GradeHistoryRepository;
import com.zoneq.domain.noise.domain.NoiseMeasurement;
import com.zoneq.domain.noise.repository.NoiseMeasurementRepository;
import com.zoneq.domain.session.domain.Session;
import com.zoneq.domain.session.repository.SessionRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final NoiseMeasurementRepository noiseMeasurementRepository;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void recalculate(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Session> sessions = sessionRepository
                .findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(userId);
        if (sessions.isEmpty()) return;

        List<Long> sessionIds = sessions.stream().map(Session::getId).toList();
        List<NoiseMeasurement> measurements = noiseMeasurementRepository.findBySessionIdIn(sessionIds);
        if (measurements.isEmpty()) return;

        List<VisitSummary> visits = buildVisitSummaries(sessions, measurements);
        if (visits.isEmpty()) return;

        GradeResult result = GradeCalculator.calculate(visits);

        if (!result.grade().equals(user.getGrade())) {
            user.updateGrade(result.grade());
            gradeHistoryRepository.save(GradeHistory.of(user, result.grade()));
            eventPublisher.publishEvent(new GradeUpdatedEvent(user.getId(), result.grade()));
        }
    }

    @Transactional(readOnly = true)
    public GradeScoreResponse getMyGrade(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return buildScoreResponse(user.getId());
    }

    @Transactional(readOnly = true)
    public List<GradeHistoryResponse> getMyHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return gradeHistoryRepository.findByUserIdOrderByChangedAtDesc(user.getId())
                .stream().map(GradeHistoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GradeDetailResponse getUserGrade(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Session> sessions = sessionRepository
                .findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(userId);
        if (sessions.isEmpty()) return GradeDetailResponse.noData(user);

        List<Long> sessionIds = sessions.stream().map(Session::getId).toList();
        List<NoiseMeasurement> measurements = noiseMeasurementRepository.findBySessionIdIn(sessionIds);
        if (measurements.isEmpty()) return GradeDetailResponse.noData(user);

        List<VisitSummary> visits = buildVisitSummaries(sessions, measurements);
        if (visits.isEmpty()) return GradeDetailResponse.noData(user);

        GradeResult result = GradeCalculator.calculate(visits);
        return GradeDetailResponse.of(user, result);
    }

    @Transactional(readOnly = true)
    public GradeDistributionResponse getDistribution() {
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : userRepository.countByGrade()) {
            String grade = (String) row[0];
            long count = (Long) row[1];
            counts.put(grade != null ? grade : "ungraded", count);
        }
        return new GradeDistributionResponse(
                counts.getOrDefault("S", 0L),
                counts.getOrDefault("A", 0L),
                counts.getOrDefault("B", 0L),
                counts.getOrDefault("C", 0L),
                counts.getOrDefault("ungraded", 0L)
        );
    }

    private GradeScoreResponse buildScoreResponse(Long userId) {
        List<Session> sessions = sessionRepository
                .findTop5ByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(userId);
        if (sessions.isEmpty()) return GradeScoreResponse.noData();

        List<Long> sessionIds = sessions.stream().map(Session::getId).toList();
        List<NoiseMeasurement> measurements = noiseMeasurementRepository.findBySessionIdIn(sessionIds);
        if (measurements.isEmpty()) return GradeScoreResponse.noData();

        List<VisitSummary> visits = buildVisitSummaries(sessions, measurements);
        if (visits.isEmpty()) return GradeScoreResponse.noData();

        return GradeScoreResponse.from(GradeCalculator.calculate(visits));
    }

    /**
     * 세션 목록(최신순)과 측정값으로 VisitSummary 리스트를 만든다.
     * GradeCalculator는 오래된 방문 순서를 기대하므로 역순으로 반환한다.
     */
    private List<VisitSummary> buildVisitSummaries(List<Session> sessions,
                                                    List<NoiseMeasurement> measurements) {
        Map<Long, List<NoiseMeasurement>> bySession = measurements.stream()
                .collect(Collectors.groupingBy(m -> m.getSession().getId()));

        List<VisitSummary> visits = new ArrayList<>();
        List<Session> oldestFirst = new ArrayList<>(sessions);
        Collections.reverse(oldestFirst);

        for (Session s : oldestFirst) {
            List<NoiseMeasurement> ms = bySession.getOrDefault(s.getId(), List.of());
            if (ms.isEmpty()) continue;
            double avgLeq  = ms.stream().mapToDouble(NoiseMeasurement::getLeqDb).average().orElse(0);
            double avgPeak = ms.stream()
                    .mapToDouble(m -> m.getPeakCount() == null ? 0 : m.getPeakCount())
                    .average().orElse(0);
            visits.add(new VisitSummary(avgLeq, avgPeak));
        }
        return visits;
    }
}
