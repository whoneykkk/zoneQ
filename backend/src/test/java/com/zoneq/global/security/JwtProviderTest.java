package com.zoneq.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void generateAccessToken_andExtractEmail() {
        String token = jwtProvider.generateAccessToken("hong@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("hong@test.com");
    }

    @Test
    void generateRefreshToken_andExtractEmail() {
        String token = jwtProvider.generateRefreshToken("hong@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("hong@test.com");
    }

    @Test
    void validateToken_returnsTrue_forValidToken() {
        String token = jwtProvider.generateAccessToken("hong@test.com");
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalse_forInvalidToken() {
        assertThat(jwtProvider.validateToken("invalid.token.here")).isFalse();
    }
}
