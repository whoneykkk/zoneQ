package com.zoneq.domain.grade.domain;

import com.zoneq.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String grade;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public static GradeHistory of(User user, String grade) {
        GradeHistory h = new GradeHistory();
        h.user = user;
        h.grade = grade;
        h.changedAt = LocalDateTime.now();
        return h;
    }
}
