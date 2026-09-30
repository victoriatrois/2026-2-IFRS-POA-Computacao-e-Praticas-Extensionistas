package ifrs.edu.avaliacao_mnr.security;

import ifrs.edu.avaliacao_mnr.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl
    ) {
        byte[] keyBytes = secret.startsWith("base64:")
                ? Decoders.BASE64.decode(secret.substring("base64:".length()))
                : secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenTtl = accessTokenTtl;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean matchesUser(String token, AuthenticatedUser user) {
        Claims claims = parseClaims(token);
        return user.getId().toString().equals(claims.getSubject())
                && user.getEmail().equalsIgnoreCase(claims.get("email", String.class));
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }
}