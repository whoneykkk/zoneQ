package com.zoneq.domain.notification.service;

import com.zoneq.domain.grade.event.GradeUpdatedEvent;
import com.zoneq.domain.noise.event.NoiseWarningEvent;
import com.zoneq.domain.notification.domain.Notification;
import com.zoneq.domain.notification.domain.NotificationType;
import com.zoneq.domain.notification.dto.NotificationListResponse;
import com.zoneq.domain.notification.dto.NotificationResponse;
import com.zoneq.domain.notification.repository.NotificationRepository;
import com.zoneq.domain.seat.event.SeatAssignedEvent;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void onGradeUpdated(GradeUpdatedEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String body = "소음 등급이 " + event.newGrade() + "등급으로 변경되었습니다.";
        notificationRepository.save(Notification.create(user, NotificationType.GRADE_UPDATED, body));
    }

    @EventListener
    @Transactional
    public void onSeatAssigned(SeatAssignedEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String body = event.zone() + "구역 " + event.seatNumber() + "번 좌석이 배정되었습니다.";
        notificationRepository.save(Notification.create(user, NotificationType.SEAT_ASSIGNED, body));
    }

    @EventListener
    @Transactional
    public void onNoiseWarning(NoiseWarningEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String body = String.format("소음 경고: %.1fdB가 측정되었습니다. 소음을 줄여 주세요.", event.leqDb());
        notificationRepository.save(Notification.create(user, NotificationType.NOISE_WARNING, body));
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<NotificationResponse> list = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).toList();
        int unreadCount = (int) notificationRepository.countByUserIdAndReadFalse(user.getId());
        return new NotificationListResponse(list, unreadCount);
    }

    @Transactional
    public void markAsRead(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        notification.markAsRead();
    }
}
