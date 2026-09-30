package ifrs.edu.avaliacao_mnr.auth;

import ifrs.edu.avaliacao_mnr.auth.dto.AuthTokenResponse;
import ifrs.edu.avaliacao_mnr.auth.dto.LoginRequest;
import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.security.JwtService;
import ifrs.edu.avaliacao_mnr.service.AuthAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthAuditService auditService;
    private HttpServletRequest request;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        auditService = mock(AuthAuditService.class);
        request = mock(HttpServletRequest.class);
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder,
                jwtService, auditService, Duration.ofDays(30));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createAccessToken(any(User.class))).thenReturn("access.jwt.token");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
    }

    @Test
    void loginIssuesAccessAndRefreshTokensAndStoresOnlyRefreshHash() {
        User user = activeEvaluator();
        when(userRepository.findByEmailIgnoreCase("evaluator@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "bcrypt-hash")).thenReturn(true);

        AuthTokenResponse response = authService.login(
                new LoginRequest(" Evaluator@Example.com ", "correct-password"), request);

        assertEquals("Bearer", response.tokenType());
        assertEquals("access.jwt.token", response.accessToken());
        assertEquals(900L, response.expiresIn());
        assertEquals("evaluator@example.com", response.user().email());
        var savedToken = forClass(RefreshToken.class);
        verify(refreshTokenRepository).saveAndFlush(savedToken.capture());
        assertEquals(sha256(response.refreshToken()), savedToken.getValue().getTokenHash());
        assertNotEquals(response.refreshToken(), savedToken.getValue().getTokenHash());
        verify(auditService).record("LOGIN", user, "evaluator@example.com", true,
                "Login succeeded", request);
    }

    @Test
    void loginRejectsWrongPasswordWithGenericAuthenticationError() {
        User user = activeEvaluator();
        when(userRepository.findByEmailIgnoreCase("evaluator@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(
                new LoginRequest("evaluator@example.com", "wrong-password"), request));

        verify(refreshTokenRepository, never()).saveAndFlush(any(RefreshToken.class));
        verify(auditService).record("LOGIN", user, "evaluator@example.com", false,
                "Invalid credentials", request);
    }

    @Test
    void refreshRotatesAndRevokesThePreviousRefreshToken() {
        User user = activeEvaluator();
        RefreshToken previous = new RefreshToken();
        previous.setUser(user);
        previous.setExpiresAt(OffsetDateTime.now().plusDays(1));
        previous.setTokenHash("persisted-hash");
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(previous));

        AuthTokenResponse response = authService.refresh("presented-refresh-token", request);

        assertEquals("access.jwt.token", response.accessToken());
        assertNotEquals("presented-refresh-token", response.refreshToken());
        assertNotNull(previous.getRevokedAt());
        verify(refreshTokenRepository).save(previous);
        verify(auditService).record("REFRESH", user, user.getEmail(), true,
                "Refresh token rotated", request);
    }

    private User activeEvaluator() {
        User user = new User();
        user.setName("Rita");
        user.setSurname("Evaluator");
        user.setEmail("evaluator@example.com");
        user.setCpf("123.456.789-00");
        user.setPasswordHash("bcrypt-hash");
        user.setRole(Role.EVALUATOR);
        user.setActive(true);
        return user;
    }

        private String sha256(String value) {
                try {
                        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
                        return HexFormat.of().formatHex(digest);
                } catch (NoSuchAlgorithmException exception) {
                        throw new AssertionError("SHA-256 is unavailable", exception);
                }
        }
}
