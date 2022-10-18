package com.maskting.backend.repository;

import com.maskting.backend.domain.QUser;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryExtensionImpl implements UserRepositoryExtension{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<User> findByLocationsAndGender(List<String> locations, String gender, List<Long> id) {
        QUser user = QUser.user;
        return jpaQueryFactory
                .selectFrom(user)
                .where(user.gender.notEqualsIgnoreCase(gender)
                    .and(user.location.in(locations))
                    .and(user.roleType.eq(RoleType.USER))
                    .and(user.id.notIn(id)))
                .fetch();
    }
}
