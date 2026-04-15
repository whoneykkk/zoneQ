package com.zoneq.domain.notification.repository;

import com.zoneq.domain.notification.domain.Notification;
import com.zoneq.domain.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    long countByTypeAndCreatedAtAfter(NotificationType type, LocalDateTime after);
}
