package com.zoneq.domain.user.domain;

import com.zoneq.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private String grade;

    private String refreshToken;

    private int loginFailureCount;

    private boolean locked;

    public static User create(String name, String email, String encodedPassword, UserRole role) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.password = encodedPassword;
        user.role = role;
        return user;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void recordLoginFailure() {
        this.loginFailureCount++;
        if (this.loginFailureCount >= 5) {
            this.locked = true;
        }
    }

    public void resetLoginFailure() {
        this.loginFailureCount = 0;
        this.locked = false;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
