package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.auth.InvalidRefreshTokenException;
import ifrs.edu.avaliacao_mnr.dto.AuthTokenResponse;
import ifrs.edu.avaliacao_mnr.dto.LoginRequest;
import ifrs.edu.avaliacao_mnr.enums.AuditEventType;
import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditEventService auditService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditEventService auditService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                auditService.record(AuditEventType.LOGIN_FAILURE, user, normalizedEmail, false,
                    "Invalid credentials", httpRequest);
            throw new BadCredentialsException("Invalid email or password");
        }
        auditService.record(AuditEventType.LOGIN_SUCCESS, user, normalizedEmail, true,
            "Login succeeded", httpRequest);
        return issueTokens(user, refreshTokenService.create(user));
    }

    @Transactional
    public AuthTokenResponse refresh(String rawRefreshToken, HttpServletRequest httpRequest) {
        RefreshTokenService.ValidationResult validation = refreshTokenService.validate(rawRefreshToken);
        RefreshToken current = validation.token();
        if (!validation.valid()) {
            User user = current == null ? null : current.getUser();
            auditService.record(AuditEventType.TOKEN_REFRESH, user, user == null ? null : user.getEmail(), false,
                    "Invalid, expired, revoked, or inactive refresh token", httpRequest);
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        String nextRefreshToken = refreshTokenService.rotate(current);
        auditService.record(AuditEventType.TOKEN_REFRESH, current.getUser(), current.getUser().getEmail(), true,
                "Refresh token rotated", httpRequest);
        return issueTokens(current.getUser(), nextRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken, HttpServletRequest httpRequest) {
        RefreshTokenService.RevocationResult result = refreshTokenService.revoke(rawRefreshToken);
        RefreshToken token = result.token();
        if (!result.revoked()) {
            auditService.record(AuditEventType.LOGOUT, token == null ? null : token.getUser(),
                    token == null ? null : token.getUser().getEmail(), false,
                    "Refresh token was invalid or already revoked", httpRequest);
            return;
        }
        auditService.record(AuditEventType.LOGOUT, token.getUser(), token.getUser().getEmail(), true,
                "Refresh token revoked", httpRequest);
    }

    public AuthTokenResponse.UserProfile profile(AuthenticatedUser user) {
        return new AuthTokenResponse.UserProfile(user.getId(), user.getName(), user.getSurname(),
                user.getEmail(), user.getRole());
    }

    private AuthTokenResponse issueTokens(User user, String rawRefreshToken) {
        return new AuthTokenResponse("Bearer", jwtService.createAccessToken(user),
                jwtService.getAccessTokenTtlSeconds(), rawRefreshToken,
                new AuthTokenResponse.UserProfile(user.getId(), user.getName(), user.getSurname(),
                        user.getEmail(), user.getRole().name()));
    }
}