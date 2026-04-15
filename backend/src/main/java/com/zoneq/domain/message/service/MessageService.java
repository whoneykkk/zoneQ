package com.zoneq.domain.message.service;

import com.zoneq.domain.message.domain.Message;
import com.zoneq.domain.message.dto.*;
import com.zoneq.domain.message.repository.MessageRepository;
import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.seat.repository.SeatRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public MessageSendResponse send(String senderEmail, MessageSendRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Seat senderSeat = seatRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        Seat receiverSeat = seatRepository.findById(request.receiverSeatId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        User receiver = receiverSeat.getUser();
        if (receiver == null) {
            throw new BusinessException(ErrorCode.SEAT_NOT_FOUND);
        }

        Message message = Message.create(sender, senderSeat, receiver,
                request.body(), request.isAnonymous());
        Message saved = messageRepository.save(message);
        return MessageSendResponse.of(saved.getId());
    }

    @Transactional
    public MessageSendResponse reply(String senderEmail, Long parentId,
                                      MessageReplyRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Seat senderSeat = seatRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        Message parent = messageRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (parent.isAnonymous()) {
            throw new BusinessException(ErrorCode.REPLY_NOT_ALLOWED);
        }

        Message reply = Message.createReply(sender, senderSeat, parent, request.body());
        Message saved = messageRepository.save(reply);
        return MessageSendResponse.of(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<MessageInboxResponse> getInbox(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return messageRepository.findByReceiverIdOrderBySentAtDesc(user.getId())
                .stream().map(MessageInboxResponse::from).toList();
    }

    @Transactional
    public MessageDetailResponse getMessage(String email, Long messageId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getReceiver().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        message.markAsRead();
        return MessageDetailResponse.from(message);
    }
}
