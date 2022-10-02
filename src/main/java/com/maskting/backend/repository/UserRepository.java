package com.maskting.backend.repository;

import com.maskting.backend.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryExtension{
    User findByProviderId(String providerId);

    Page<User> findBySort(boolean sort, Pageable pageable);

    int countByNameContains(String name);

    Page<User> findByNameContains(String name, PageRequest pageRequest);

    User findByName(String name);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.latest = :latest")
    void updateAllUserLatest(@Param("latest") boolean latest);
}
