package com.maskting.backend.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class CookieUtil {
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (isExistCookie(cookies)) {
            return findGetCookie(name, cookies);
        }
        return Optional.empty();
    }

    private static Optional<Cookie> findGetCookie(String name, Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (isEquals(name, cookie)) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }

    private static boolean isExistCookie(Cookie[] cookies) {
        return cookies != null && cookies.length > 0;
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();

        if (isExistCookie(cookies)) {
            findDeleteCookie(response, name, cookies);
        }
    }

    private static void findDeleteCookie(HttpServletResponse response, String name, Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (isEquals(name, cookie)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    private static boolean isEquals(String name, Cookie cookie) {
        return name.equals(cookie.getName());
    }
}
