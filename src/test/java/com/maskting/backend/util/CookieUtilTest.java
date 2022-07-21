package com.maskting.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    @InjectMocks
    CookieUtil cookieUtil;

    @Test
    @DisplayName("쿠키 반환")
    void getCookie() {
        Cookie[] cookies = new Cookie[10];
        Cookie cookie = createCookie();
        cookies[0] = cookie;
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getCookies()).willReturn(cookies);

        Cookie getCookie = cookieUtil.getCookie(request, "test_name").get();

        assertEquals(cookie, getCookie);
    }

    private Cookie createCookie() {
        Cookie cookie = new Cookie("test_name", "test_value");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(100000);
        return cookie;
    }

    @Test
    @DisplayName("쿠키 추가")
    void addCookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(response.getHeader("Cookie")).willReturn("test");

        cookieUtil.addCookie(response, "test_name", "token_idx", 10000);

        assertEquals("test", response.getHeader("Cookie"));
    }

    @Test
    @DisplayName("쿠키 삭제")
    void deleteCookie() {
        Cookie[] cookies = new Cookie[10];
        Cookie cookie = createCookie();
        cookies[0] = cookie;
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(request.getCookies()).willReturn(cookies);
        given(response.getHeader("Cookie")).willReturn(null);

        cookieUtil.deleteCookie(request, response, "test_name");

        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertNull(response.getHeader("Cookie"));
    }
}