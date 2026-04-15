package com.zoneq.domain.notice.domain;

import com.zoneq.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Notice create(User admin, String title, String body, boolean pinned) {
        Notice n = new Notice();
        n.admin = admin;
        n.title = title;
        n.body = body;
        n.pinned = pinned;
        n.createdAt = LocalDateTime.now();
        return n;
    }

    public void update(String title, String body, Boolean pinned) {
        if (title != null) this.title = title;
        if (body != null) this.body = body;
        if (pinned != null) this.pinned = pinned;
    }
}
