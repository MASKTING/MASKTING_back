package com.maskting.backend.repository;


import com.maskting.backend.domain.User;

import java.util.List;

public interface UserRepositoryExtension {
    List<User> findByLocationsAndGender(List<String> locations, String gender);
}
