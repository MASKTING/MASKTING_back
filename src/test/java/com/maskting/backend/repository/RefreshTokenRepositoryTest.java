package com.maskting.backend.repository;

import com.maskting.backend.domain.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("refreshToken 저장")
    public void save() {
        String id = "key";
        String value = "testProviderId";
        RefreshToken refreshToken = new RefreshToken(id, value);

        RefreshToken dbRefreshToken = refreshTokenRepository.save(refreshToken);

        assertEquals(id, dbRefreshToken.getId());
        assertEquals(value, dbRefreshToken.getProviderId());
    }

    @Test
    @DisplayName("refreshToken 삭제")
    public void delete() {
        String id = "key";
        String value = "testProviderId";
        RefreshToken refreshToken = new RefreshToken(id, value);
        RefreshToken dbRefreshToken = refreshTokenRepository.save(refreshToken);

        assertNotNull(refreshTokenRepository.findById(id).get());
        refreshTokenRepository.delete(dbRefreshToken);

        assertNull(refreshTokenRepository.findById(id).orElse(null));
    }
}