package com.zoneq.domain.seat.domain;

import com.zoneq.domain.user.domain.User;
import com.zoneq.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"zone", "seat_number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1)
    private String zone;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Seat of(String zone, int seatNumber) {
        Seat seat = new Seat();
        seat.zone = zone;
        seat.seatNumber = seatNumber;
        return seat;
    }

    public void assign(User user) {
        this.user = user;
        this.status = SeatStatus.OCCUPIED;
    }

    public void release() {
        this.user = null;
        this.status = SeatStatus.AVAILABLE;
    }
}
