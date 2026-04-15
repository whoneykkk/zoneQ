package com.zoneq.domain.message.domain;

import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(nullable = false)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Message parent;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public static Message create(User sender, Seat senderSeat, User receiver,
                                  String body, boolean isAnonymous) {
        Message m = new Message();
        m.sender = sender;
        m.seat = isAnonymous ? null : senderSeat;
        m.receiver = receiver;
        m.body = body;
        m.isAnonymous = isAnonymous;
        m.sentAt = LocalDateTime.now();
        return m;
    }

    public static Message createReply(User sender, Seat senderSeat,
                                       Message parent, String body) {
        Message m = new Message();
        m.sender = sender;
        m.seat = senderSeat;
        m.receiver = parent.getSender();
        m.body = body;
        m.isAnonymous = false;
        m.parent = parent;
        m.sentAt = LocalDateTime.now();
        return m;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
