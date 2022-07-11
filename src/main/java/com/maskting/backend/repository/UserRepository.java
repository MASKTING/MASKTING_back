package com.maskting.backend.repository;

import com.maskting.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Long, User> {
    User findByProviderId(String providerId);
}
