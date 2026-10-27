package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${app.security.jwt.refresh-token-ttl:P30D}") Duration refreshTokenTtl) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public String create(User user) {
        return persistNewToken(user).rawToken();
    }

    @Transactional
    public ValidationResult validate(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken)).orElse(null);
        boolean valid = token != null && token.getRevokedAt() == null
                && token.getExpiresAt().isAfter(OffsetDateTime.now())
                && token.getUser().isActive();
        return new ValidationResult(token, valid);
    }

    @Transactional
    public String rotate(RefreshToken previousToken) {
        IssuedToken issued = persistNewToken(previousToken.getUser());
        previousToken.setRevokedAt(OffsetDateTime.now());
        previousToken.setReplacedByTokenId(issued.token().getId());
        refreshTokenRepository.save(previousToken);
        return issued.rawToken();
    }

    @Transactional
    public RevocationResult revoke(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken)).orElse(null);
        if (token == null || token.getRevokedAt() != null) {
            return new RevocationResult(token, false);
        }
        token.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(token);
        return new RevocationResult(token, true);
    }

    private IssuedToken persistNewToken(User user) {
        String rawToken = createOpaqueToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(OffsetDateTime.now().plus(refreshTokenTtl));
        return new IssuedToken(rawToken, refreshTokenRepository.saveAndFlush(token));
    }

    private String createOpaqueToken() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record ValidationResult(RefreshToken token, boolean valid) {
    }

    public record RevocationResult(RefreshToken token, boolean revoked) {
    }

    private record IssuedToken(String rawToken, RefreshToken token) {
    }
}