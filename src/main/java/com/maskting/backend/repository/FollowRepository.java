package com.maskting.backend.repository;

import com.maskting.backend.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("select f from Follow f where f.following.id = :followingId and f.follower.id = :followerId")
    Optional<Follow> findByFollowingAndFollower(@Param("followingId") Long followingId, @Param("followerId") Long followerId);
}
