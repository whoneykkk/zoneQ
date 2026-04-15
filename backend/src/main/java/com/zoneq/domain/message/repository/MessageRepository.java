package com.zoneq.domain.message.repository;

import com.zoneq.domain.message.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId);
}
