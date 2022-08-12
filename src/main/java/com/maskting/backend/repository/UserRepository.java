package com.maskting.backend.repository;

import com.maskting.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByProviderId(String providerId);

    Page<User> findBySort(boolean sort, Pageable pageable);
}
