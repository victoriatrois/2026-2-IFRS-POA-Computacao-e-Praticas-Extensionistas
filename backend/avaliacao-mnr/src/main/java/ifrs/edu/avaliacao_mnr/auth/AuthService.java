package ifrs.edu.avaliacao_mnr.auth;

import ifrs.edu.avaliacao_mnr.auth.dto.AuthTokenResponse;
import ifrs.edu.avaliacao_mnr.auth.dto.LoginRequest;
import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.security.AuthenticatedUser;
import ifrs.edu.avaliacao_mnr.security.JwtService;
import ifrs.edu.avaliacao_mnr.service.AuthAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthAuditService auditService;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthAuditService auditService,
                       @Value("${app.security.jwt.refresh-token-ttl:P30D}") Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.record("LOGIN", user, normalizedEmail, false, "Invalid credentials", httpRequest);
            throw new BadCredentialsException("Authentication credentials do not match");
        }
        auditService.record("LOGIN", user, normalizedEmail, true, "Login succeeded", httpRequest);
        return issueTokens(user, null);
    }

    @Transactional
    public AuthTokenResponse refresh(String rawRefreshToken, HttpServletRequest httpRequest) {
        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken)).orElse(null);
        if (current == null) {
            auditService.record("REFRESH", null, null, false, "Unknown refresh token", httpRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(OffsetDateTime.now())
                || !current.getUser().isActive()) {
            auditService.record("REFRESH", current.getUser(), current.getUser().getEmail(), false,
                    "Invalid, expired, revoked, or inactive refresh token", httpRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        AuthTokenResponse response = issueTokens(current.getUser(), current);
        auditService.record("REFRESH", current.getUser(), current.getUser().getEmail(), true,
                "Refresh token rotated", httpRequest);
        return response;
    }

    @Transactional
    public void logout(String rawRefreshToken, HttpServletRequest httpRequest) {
        RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken)).orElse(null);
        if (token == null || token.getRevokedAt() != null) {
            auditService.record("LOGOUT", token == null ? null : token.getUser(),
                    token == null ? null : token.getUser().getEmail(), false,
                    "Refresh token was invalid or already revoked", httpRequest);
            return;
        }
        token.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(token);
        auditService.record("LOGOUT", token.getUser(), token.getUser().getEmail(), true,
                "Refresh token revoked", httpRequest);
    }

    public AuthTokenResponse.UserProfile profile(AuthenticatedUser user) {
        return new AuthTokenResponse.UserProfile(user.getId(), user.getName(), user.getSurname(),
                user.getEmail(), user.getRole());
    }

    private AuthTokenResponse issueTokens(User user, RefreshToken previousToken) {
        String rawToken = createOpaqueToken();
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setTokenHash(hash(rawToken));
        newToken.setExpiresAt(OffsetDateTime.now().plus(refreshTokenTtl));
        newToken = refreshTokenRepository.saveAndFlush(newToken);

        if (previousToken != null) {
            previousToken.setRevokedAt(OffsetDateTime.now());
            previousToken.setReplacedByTokenId(newToken.getId());
            refreshTokenRepository.save(previousToken);
        }

        return new AuthTokenResponse("Bearer", jwtService.createAccessToken(user),
                jwtService.getAccessTokenTtlSeconds(), rawToken,
                new AuthTokenResponse.UserProfile(user.getId(), user.getName(), user.getSurname(),
                        user.getEmail(), user.getRole().name()));
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
}
