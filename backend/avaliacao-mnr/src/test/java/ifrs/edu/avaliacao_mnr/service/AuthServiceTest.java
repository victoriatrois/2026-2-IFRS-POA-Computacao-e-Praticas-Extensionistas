package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.AuthTokenResponse;
import ifrs.edu.avaliacao_mnr.dto.LoginRequest;
import ifrs.edu.avaliacao_mnr.enums.AuditEventType;
import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuditEventService auditService;
    private HttpServletRequest request;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenService = mock(RefreshTokenService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        auditService = mock(AuditEventService.class);
        request = mock(HttpServletRequest.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService, auditService, refreshTokenService);
        when(refreshTokenService.create(any(User.class))).thenReturn("issued-refresh-token");
        when(jwtService.createAccessToken(any(User.class))).thenReturn("access.jwt.token");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
    }

    @Test
    void loginIssuesAccessAndRefreshTokens() {
        User user = activeEvaluator();
        when(userRepository.findByEmailIgnoreCase("evaluator@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "bcrypt-hash")).thenReturn(true);

        AuthTokenResponse response = authService.login(
                new LoginRequest(" Evaluator@Example.com ", "correct-password"), request);

        assertEquals("Bearer", response.tokenType());
        assertEquals("access.jwt.token", response.accessToken());
        assertEquals(900L, response.expiresIn());
        assertEquals("evaluator@example.com", response.user().email());
        assertEquals("issued-refresh-token", response.refreshToken());
        verify(refreshTokenService).create(user);
        verify(auditService).record(AuditEventType.LOGIN_SUCCESS, user, "evaluator@example.com", true,
                "Login succeeded", request);
    }

    @Test
    void loginRejectsWrongPasswordWithGenericAuthenticationError() {
        User user = activeEvaluator();
        when(userRepository.findByEmailIgnoreCase("evaluator@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(
                new LoginRequest("evaluator@example.com", "wrong-password"), request));

        verify(refreshTokenService, never()).create(any(User.class));
        verify(auditService).record(AuditEventType.LOGIN_FAILURE, user, "evaluator@example.com", false,
                "Invalid credentials", request);
    }

    @Test
    void refreshDelegatesValidationAndRotation() {
        User user = activeEvaluator();
        RefreshToken previous = new RefreshToken();
        previous.setUser(user);
        when(refreshTokenService.validate("presented-refresh-token"))
                .thenReturn(new RefreshTokenService.ValidationResult(previous, true));
        when(refreshTokenService.rotate(previous)).thenReturn("rotated-refresh-token");

        AuthTokenResponse response = authService.refresh("presented-refresh-token", request);

        assertEquals("access.jwt.token", response.accessToken());
        assertNotEquals("presented-refresh-token", response.refreshToken());
        assertEquals("rotated-refresh-token", response.refreshToken());
        verify(refreshTokenService).rotate(previous);
        verify(auditService).record(AuditEventType.TOKEN_REFRESH, user, user.getEmail(), true,
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
}