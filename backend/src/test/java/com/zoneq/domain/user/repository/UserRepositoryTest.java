package com.zoneq.domain.user.repository;

import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenExists() {
        User user = User.create("홍길동", "hong@test.com", "password123", UserRole.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("hong@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotExists() {
        Optional<User> found = userRepository.findByEmail("notexist@test.com");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_whenEmailExists() {
        User user = User.create("김철수", "kim@test.com", "password123", UserRole.USER);
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("kim@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@test.com")).isFalse();
    }
}
