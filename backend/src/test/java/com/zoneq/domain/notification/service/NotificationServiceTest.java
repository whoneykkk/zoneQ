package com.zoneq.domain.notification.service;

import com.zoneq.domain.grade.event.GradeUpdatedEvent;
import com.zoneq.domain.noise.event.NoiseWarningEvent;
import com.zoneq.domain.notification.domain.Notification;
import com.zoneq.domain.notification.domain.NotificationType;
import com.zoneq.domain.notification.dto.NotificationListResponse;
import com.zoneq.domain.notification.repository.NotificationRepository;
import com.zoneq.domain.seat.event.SeatAssignedEvent;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create("테스터", "test@test.com", "pw", UserRole.USER);
    }

    @Test
    void onGradeUpdated_savesGradeUpdatedNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificationService.onGradeUpdated(new GradeUpdatedEvent(1L, "A"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.GRADE_UPDATED);
        assertThat(captor.getValue().getBody()).contains("A");
    }

    @Test
    void onSeatAssigned_savesSeatAssignedNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificationService.onSeatAssigned(new SeatAssignedEvent(1L, "S", 3));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.SEAT_ASSIGNED);
        assertThat(captor.getValue().getBody()).contains("S").contains("3");
    }

    @Test
    void onNoiseWarning_savesNoiseWarningNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        notificationService.onNoiseWarning(new NoiseWarningEvent(1L, 63.5));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.NOISE_WARNING);
        assertThat(captor.getValue().getBody()).contains("63.5");
    }

    @Test
    void getMyNotifications_returnsListAndUnreadCount() {
        Notification n1 = Notification.create(mockUser, NotificationType.GRADE_UPDATED, "등급 변경");
        Notification n2 = Notification.create(mockUser, NotificationType.SEAT_ASSIGNED, "좌석 배정");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(n1, n2));
        when(notificationRepository.countByUserIdAndReadFalse(any())).thenReturn(2L);

        NotificationListResponse result = notificationService.getMyNotifications("test@test.com");

        assertThat(result.notifications()).hasSize(2);
        assertThat(result.unreadCount()).isEqualTo(2);
    }

    @Test
    void markAsRead_marksNotification() {
        User spyUser = spy(mockUser);
        doReturn(10L).when(spyUser).getId();
        Notification n = Notification.create(spyUser, NotificationType.SEAT_ASSIGNED, "좌석 배정");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(spyUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.markAsRead("test@test.com", 1L);

        assertThat(n.isRead()).isTrue();
    }

    @Test
    void markAsRead_throwsForbidden_whenNotOwner() {
        User otherUser = spy(User.create("다른유저", "other@test.com", "pw", UserRole.USER));
        doReturn(99L).when(otherUser).getId();

        User requestUser = spy(mockUser);
        doReturn(10L).when(requestUser).getId();

        Notification n = Notification.create(otherUser, NotificationType.GRADE_UPDATED, "등급");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(requestUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> notificationService.markAsRead("test@test.com", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void markAsRead_throwsNotFound_whenMissing() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("test@test.com", 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }
}
