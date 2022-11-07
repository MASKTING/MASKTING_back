package com.maskting.backend.repository;

import com.maskting.backend.domain.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    List<ChatUser> findAllByUserId(Long userId);
}
