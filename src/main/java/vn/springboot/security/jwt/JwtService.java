package vn.springboot.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.security.JwtProperties;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates stateless access tokens (HS512 signed JWTs).
 * Refresh tokens are handled separately by the auth service (DB-backed).
 *
 * <p>Besides the {@code authorities} claim (kept for Spring Security interop),
 * the token carries the single {@code role} plus basic identity claims
 * ({@code uid}, {@code email}, {@code name}) so a front-end can build
 * menus/guards straight from the decoded token without an extra round-trip.
 */
@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_AUTHORITIES = "authorities";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";

    /** HS512 (the algorithm this service signs with) requires a >= 512-bit (64-byte) key. */
    private static final int MIN_HS512_KEY_BYTES = 64;

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret (APP_JWT_SECRET) is not configured — refusing to start.");
        }
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        if (keyBytes.length < MIN_HS512_KEY_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret (APP_JWT_SECRET) decodes to " + keyBytes.length
                            + " bytes, but HS512 requires at least " + MIN_HS512_KEY_BYTES
                            + " bytes (512 bits) — refusing to start. Generate one with e.g. `openssl rand -base64 64`.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        // The authorities (single ROLE_<name>) are what Spring Security consumes; kept for interop.
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(userDetails.getUsername())
                .claim(CLAIM_AUTHORITIES, authorities)
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getAccessTokenExpiration()));

        // Enrich with identity + decoded role when we have the full user entity
        // (always true for our login/refresh flows).
        if (userDetails instanceof CustomUserDetails principal) {
            UserEntity user = principal.getUser();
            builder.claim(CLAIM_UID, user.getId())
                    .claim(CLAIM_EMAIL, user.getEmail())
                    .claim(CLAIM_NAME, user.getName())
                    .claim(CLAIM_ROLE, user.getRole().name());
        }

        return builder.signWith(signingKey).compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** Returns true only if the token is well-formed, unexpired and belongs to the given user. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            return userDetails.getUsername().equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
