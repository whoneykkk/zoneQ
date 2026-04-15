package com.zoneq.domain.noise.domain;

import com.zoneq.domain.seat.domain.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "calibration_maps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalibrationMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_seat_id", nullable = false)
    private Seat sourceSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_seat_id", nullable = false)
    private Seat receiverSeat;

    @Column(name = "attenuation_db", nullable = false)
    private Double attenuationDb;

    @Column(name = "calibrated_at", nullable = false)
    private LocalDateTime calibratedAt;

    public static CalibrationMap of(Seat sourceSeat, Seat receiverSeat, Double attenuationDb) {
        CalibrationMap map = new CalibrationMap();
        map.sourceSeat = sourceSeat;
        map.receiverSeat = receiverSeat;
        map.attenuationDb = attenuationDb;
        map.calibratedAt = LocalDateTime.now();
        return map;
    }
}
