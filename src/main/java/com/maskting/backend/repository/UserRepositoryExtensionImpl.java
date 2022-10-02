package com.maskting.backend.repository;

import com.maskting.backend.domain.QUser;
import com.maskting.backend.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryExtensionImpl implements  UserRepositoryExtension{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<User> findByLocationsAndGender(List<String> locations, String gender) {
        QUser user = QUser.user;
        return jpaQueryFactory.selectFrom(user).where(user.gender.notEqualsIgnoreCase(gender)
                .and(user.location.in(locations))).fetch();
    }
}
