package com.zoneq.domain.message.service;

import com.zoneq.domain.message.domain.Message;
import com.zoneq.domain.message.dto.*;
import com.zoneq.domain.message.repository.MessageRepository;
import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.domain.SeatStatus;
import com.zoneq.domain.seat.repository.SeatRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private User receiver;
    private Seat senderSeat;
    private Seat receiverSeat;

    @BeforeEach
    void setUp() {
        sender   = User.create("발신자", "sender@test.com", "pw", UserRole.USER);
        receiver = User.create("수신자", "receiver@test.com", "pw", UserRole.USER);

        senderSeat = mock(Seat.class);
        lenient().when(senderSeat.getZone()).thenReturn("A");
        lenient().when(senderSeat.getSeatNumber()).thenReturn(1);

        receiverSeat = mock(Seat.class);
        lenient().when(receiverSeat.getUser()).thenReturn(receiver);
        lenient().when(receiverSeat.getStatus()).thenReturn(SeatStatus.OCCUPIED);
    }

    // ── send ─────────────────────────────────────────────────────────

    @Test
    void send_success_anonymousMessage_seatIsNull() {
        MessageSendRequest req = new MessageSendRequest(2L, "조용히 해주세요", true);

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.of(senderSeat));
        when(seatRepository.findById(2L)).thenReturn(Optional.of(receiverSeat));
        when(messageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        messageService.send("sender@test.com", req);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().isAnonymous()).isTrue();
        assertThat(captor.getValue().getSeat()).isNull();
    }

    @Test
    void send_success_namedMessage_seatIsSet() {
        MessageSendRequest req = new MessageSendRequest(2L, "안녕하세요", false);

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.of(senderSeat));
        when(seatRepository.findById(2L)).thenReturn(Optional.of(receiverSeat));
        when(messageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        messageService.send("sender@test.com", req);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().isAnonymous()).isFalse();
        assertThat(captor.getValue().getSeat()).isEqualTo(senderSeat);
    }

    @Test
    void send_throwsSeatNotFound_whenSenderNotSeated() {
        MessageSendRequest req = new MessageSendRequest(2L, "조용히", true);

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.send("sender@test.com", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SEAT_NOT_FOUND.getMessage());
    }

    @Test
    void send_throwsSeatNotFound_whenReceiverSeatEmpty() {
        MessageSendRequest req = new MessageSendRequest(2L, "조용히", true);

        Seat emptySeat = mock(Seat.class);
        when(emptySeat.getUser()).thenReturn(null);

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.of(senderSeat));
        when(seatRepository.findById(2L)).thenReturn(Optional.of(emptySeat));

        assertThatThrownBy(() -> messageService.send("sender@test.com", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SEAT_NOT_FOUND.getMessage());
    }

    // ── reply ────────────────────────────────────────────────────────

    @Test
    void reply_success_whenParentIsNamed() {
        Message parent = mock(Message.class);
        when(parent.isAnonymous()).thenReturn(false);
        when(parent.getSender()).thenReturn(receiver);

        MessageReplyRequest req = new MessageReplyRequest("죄송합니다");

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.of(senderSeat));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(messageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        messageService.reply("sender@test.com", 1L, req);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().isAnonymous()).isFalse();
        assertThat(captor.getValue().getReceiver()).isEqualTo(receiver);
    }

    @Test
    void reply_throwsReplyNotAllowed_whenParentIsAnonymous() {
        Message parent = mock(Message.class);
        when(parent.isAnonymous()).thenReturn(true);

        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.of(senderSeat));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> messageService.reply("sender@test.com", 1L, new MessageReplyRequest("ㅈㅅ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.REPLY_NOT_ALLOWED.getMessage());
    }

    @Test
    void reply_throwsMessageNotFound_whenParentNotExist() {
        when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));
        when(seatRepository.findByUserId(any())).thenReturn(Optional.of(senderSeat));
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.reply("sender@test.com", 999L, new MessageReplyRequest("ㅈㅅ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MESSAGE_NOT_FOUND.getMessage());
    }

    // ── getInbox ─────────────────────────────────────────────────────

    @Test
    void getInbox_anonymousMessage_senderSeatIsHidden() {
        Seat namedSeat = mock(Seat.class);
        when(namedSeat.getZone()).thenReturn("B");
        when(namedSeat.getSeatNumber()).thenReturn(3);

        Message anonMsg = mock(Message.class);
        when(anonMsg.getId()).thenReturn(1L);
        when(anonMsg.isAnonymous()).thenReturn(true);
        when(anonMsg.getBody()).thenReturn("조용히");
        when(anonMsg.isRead()).thenReturn(false);
        when(anonMsg.getSentAt()).thenReturn(LocalDateTime.now());

        Message namedMsg = mock(Message.class);
        when(namedMsg.getId()).thenReturn(2L);
        when(namedMsg.isAnonymous()).thenReturn(false);
        when(namedMsg.getSeat()).thenReturn(namedSeat);
        when(namedMsg.getBody()).thenReturn("안녕");
        when(namedMsg.isRead()).thenReturn(false);
        when(namedMsg.getSentAt()).thenReturn(LocalDateTime.now());

        when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(receiver));
        when(messageRepository.findByReceiverIdOrderBySentAtDesc(any()))
                .thenReturn(List.of(anonMsg, namedMsg));

        List<MessageInboxResponse> result = messageService.getInbox("receiver@test.com");

        assertThat(result.get(0).senderSeat()).isEqualTo("익명");
        assertThat(result.get(0).canReply()).isFalse();
        assertThat(result.get(1).senderSeat()).isEqualTo("B-3");
        assertThat(result.get(1).canReply()).isTrue();
    }

    // ── getMessage ───────────────────────────────────────────────────

    @Test
    void getMessage_markAsRead_andReturnsDetail() {
        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(1L);

        Message msg = mock(Message.class);
        when(msg.getId()).thenReturn(1L);
        when(msg.getReceiver()).thenReturn(currentUser);
        when(msg.isAnonymous()).thenReturn(false);
        when(msg.getSeat()).thenReturn(senderSeat);
        when(msg.getBody()).thenReturn("안녕");
        when(msg.isRead()).thenReturn(true);
        when(msg.getSentAt()).thenReturn(LocalDateTime.now());

        when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        MessageDetailResponse result = messageService.getMessage("receiver@test.com", 1L);

        verify(msg).markAsRead();
        assertThat(result.canReply()).isTrue();
        assertThat(result.senderSeat()).isEqualTo("A-1");
    }

    @Test
    void getMessage_throwsForbidden_whenNotReceiver() {
        User stranger = mock(User.class);
        when(stranger.getId()).thenReturn(99L);

        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(1L);

        Message msg = mock(Message.class);
        when(msg.getReceiver()).thenReturn(stranger);

        when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThatThrownBy(() -> messageService.getMessage("receiver@test.com", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
    }
}
