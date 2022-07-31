package com.maskting.backend.repository;

import com.maskting.backend.domain.Profile;
import com.maskting.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
