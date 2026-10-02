package ifrs.edu.avaliacao_mnr.security;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String OTHER_SECRET = "abcdef0123456789abcdef0123456789";

    private SecretKey signingKey;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        jwtService = new JwtService(SECRET, Duration.ofMinutes(15));
    }

    @Test
    void accessTokenContainsExpectedIdentityAndRoleClaims() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("evaluator@example.com");
        user.setRole(Role.EVALUATOR);

        Claims claims = jwtService.parseClaims(jwtService.createAccessToken(user));

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("evaluator@example.com", claims.get("email", String.class));
        assertEquals(Role.EVALUATOR.name(), claims.get("role", String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(900L, (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000);
    }

    @Test
    void parseClaimsRejectsTamperedSignature() {
        String[] segments = jwtService.createAccessToken(user()).split("\\.");
        char firstSignatureCharacter = segments[2].charAt(0);
        segments[2] = (firstSignatureCharacter == 'A' ? "B" : "A") + segments[2].substring(1);

        assertThrows(SignatureException.class, () -> jwtService.parseClaims(String.join(".", segments)));
    }

    @Test
    void parseClaimsRejectsExpiredToken() {
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(signingKey)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.parseClaims(token));
    }

    @Test
    void parseClaimsRejectsMalformedClaimsPayload() {
        String token = signPayload("{");

        assertThrows(JwtException.class, () -> jwtService.parseClaims(token));
    }

    @Test
    void parseClaimsRejectsTokenSignedWithDifferentSecret() {
        SecretKey otherKey = Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .signWith(otherKey)
                .compact();

        assertThrows(SignatureException.class, () -> jwtService.parseClaims(token));
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("evaluator@example.com");
        user.setRole(Role.EVALUATOR);
        return user;
    }

    private String signPayload(String payload) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
        String encodedPayload = encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + encodedPayload;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            String signature = encoder.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)));
            return signingInput + "." + signature;
        } catch (GeneralSecurityException exception) {
            throw new AssertionError("HmacSHA256 is unavailable", exception);
        }
    }
}