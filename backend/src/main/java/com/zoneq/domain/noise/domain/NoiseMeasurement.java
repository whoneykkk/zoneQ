package com.zoneq.domain.noise.domain;

import com.zoneq.domain.seat.domain.Seat;
import com.zoneq.domain.session.domain.Session;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "noise_measurements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoiseMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "leq_db", nullable = false)
    private Double leqDb;

    @Column(name = "peak_count")
    private Integer peakCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "noise_category")
    private NoiseCategory noiseCategory;

    @Column(name = "is_habitual")
    private Boolean isHabitual;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    public static NoiseMeasurement of(Session session, Seat seat,
                                      Double leqDb, Integer peakCount,
                                      NoiseCategory noiseCategory,
                                      Boolean isHabitual,
                                      LocalDateTime measuredAt) {
        NoiseMeasurement m = new NoiseMeasurement();
        m.session = session;
        m.seat = seat;
        m.leqDb = leqDb;
        m.peakCount = peakCount;
        m.noiseCategory = noiseCategory;
        m.isHabitual = isHabitual;
        m.measuredAt = measuredAt;
        return m;
    }
}
