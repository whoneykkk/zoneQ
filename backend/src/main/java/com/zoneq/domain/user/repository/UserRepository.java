package com.zoneq.domain.user.repository;

import com.zoneq.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u.grade, COUNT(u) FROM User u GROUP BY u.grade")
    List<Object[]> countByGrade();
}
