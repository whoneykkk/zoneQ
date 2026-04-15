package com.zoneq.domain.session.service;

import com.zoneq.domain.grade.service.GradeService;
import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.domain.SeatStatus;
import com.zoneq.domain.seat.dto.SeatAssignResponse;
import com.zoneq.domain.seat.repository.SeatRepository;
import com.zoneq.domain.session.domain.Session;
import com.zoneq.domain.session.repository.SessionRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;
    private final GradeService gradeService;

    @Transactional
    public SeatAssignResponse assign(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (seatRepository.findByUserId(user.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_HAS_SEAT);
        }

        String targetZone = resolveZone(user.getGrade());

        Seat seat = seatRepository.findByZoneAndStatus(targetZone, SeatStatus.AVAILABLE)
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_SEAT_AVAILABLE));

        seat.assign(user);
        Session session = sessionRepository.save(Session.start(user, seat));

        return SeatAssignResponse.of(seat, session.getId());
    }

    @Transactional
    public void release(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Session session = sessionRepository.findByUserIdAndEndedAtIsNull(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        session.end();

        Seat seat = seatRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        seat.release();

        gradeService.recalculate(user.getId());
    }

    private String resolveZone(String grade) {
        if (grade == null) return "B";
        return switch (grade) {
            case "S" -> "S";
            case "A" -> "A";
            case "B" -> "B";
            case "C" -> "C";
            default -> "B";
        };
    }
}
