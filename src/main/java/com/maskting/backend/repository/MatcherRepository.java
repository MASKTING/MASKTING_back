package com.maskting.backend.repository;

import com.maskting.backend.domain.Matcher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatcherRepository extends JpaRepository<Matcher, Long> {
}
