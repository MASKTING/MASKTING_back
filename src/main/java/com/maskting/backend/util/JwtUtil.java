package com.maskting.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${app.auth.accessTokenExpiry}")
    private long accessTokenValidTime;

    @Value("${app.auth.refreshTokenExpiry}")
    private long refreshTokenValidTime;

    private static final String AUTHORITIES_KEY = "role";

    public void updateJwtUtil(String secretKey, long accessTokenValidTime, long refreshTokenValidTime) {
        this.secretKey = secretKey;
        this.accessTokenValidTime = accessTokenValidTime;
        this.refreshTokenValidTime = refreshTokenValidTime;
    }

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String providerId, String role) {
        Claims claims = Jwts.claims().setSubject(providerId);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .claim(AUTHORITIES_KEY, role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidTime))
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String key) {
        Claims claims = Jwts.claims().setSubject(key);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsJws(token).getBody();
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(new String[]{claims.get(AUTHORITIES_KEY).toString()})
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        User user = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(user, "", authorities);
    }

    public String getSubject(String token) {
        try {
            return getClaimsJws(token).getBody().getSubject();
        } catch(ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    public Jws<Claims> getClaimsJws(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey(secretKey)).build().parseClaimsJws(token);
    }

    public Boolean isTokenExpired(String token) {
        Jws<Claims> claims = getClaimsJws(token);
        return !claims.getBody().getExpiration().before(new Date());
    }

    public boolean validateToken(String token) {
        return getClaimsJws(token).getBody() != null;
    }

    public int getRefreshTokenValidTime() {
        return (int) refreshTokenValidTime;
    }

    public String resolveToken(HttpServletRequest req) {
        return req.getHeader("accessToken");
    }
}
