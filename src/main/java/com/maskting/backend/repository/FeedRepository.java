package com.maskting.backend.repository;

import com.maskting.backend.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {
}
