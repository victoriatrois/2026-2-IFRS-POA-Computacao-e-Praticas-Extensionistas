package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, Duration.ofDays(30));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createReturnsOpaqueTokenAndPersistsOnlyItsHash() {
        User user = activeUser();
        var savedToken = org.mockito.ArgumentCaptor.forClass(RefreshToken.class);

        String rawToken = refreshTokenService.create(user);

        verify(refreshTokenRepository).saveAndFlush(savedToken.capture());
        assertNotEquals(rawToken, savedToken.getValue().getTokenHash());
        assertEquals(sha256(rawToken), savedToken.getValue().getTokenHash());
        assertEquals(user, savedToken.getValue().getUser());
        assertTrue(savedToken.getValue().getExpiresAt().isAfter(OffsetDateTime.now()));
    }

    @Test
    void validateRejectsExpiredRevokedAndInactiveUserTokens() {
        User activeUser = activeUser();
        RefreshToken expired = token(activeUser, OffsetDateTime.now().minusSeconds(1), null);
        RefreshToken revoked = token(activeUser, OffsetDateTime.now().plusDays(1), OffsetDateTime.now());
        User inactiveUser = activeUser();
        inactiveUser.setActive(false);
        RefreshToken inactive = token(inactiveUser, OffsetDateTime.now().plusDays(1), null);
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString()))
                .thenReturn(Optional.of(expired), Optional.of(revoked), Optional.of(inactive));

        assertFalse(refreshTokenService.validate("expired-token").valid());
        assertFalse(refreshTokenService.validate("revoked-token").valid());
        assertFalse(refreshTokenService.validate("inactive-token").valid());
    }

    @Test
    void validateAcceptsUnexpiredUnrevokedTokenForActiveUser() {
        RefreshToken token = token(activeUser(), OffsetDateTime.now().plusDays(1), null);
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        RefreshTokenService.ValidationResult result = refreshTokenService.validate("valid-token");

        assertTrue(result.valid());
        assertEquals(token, result.token());
    }

    @Test
    void rotateRevokesPreviousTokenAndLinksReplacement() {
        User user = activeUser();
        RefreshToken previous = token(user, OffsetDateTime.now().plusDays(1), null);
        UUID replacementId = UUID.randomUUID();
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", replacementId);
            return saved;
        });

        String nextToken = refreshTokenService.rotate(previous);

        assertNotNull(nextToken);
        assertNotNull(previous.getRevokedAt());
        assertEquals(replacementId, previous.getReplacedByTokenId());
        verify(refreshTokenRepository).save(previous);
    }

    @Test
    void revokeMarksTokenAndPersistsRevocation() {
        RefreshToken token = token(activeUser(), OffsetDateTime.now().plusDays(1), null);
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        RefreshTokenService.RevocationResult result = refreshTokenService.revoke("valid-token");

        assertTrue(result.revoked());
        assertNotNull(token.getRevokedAt());
        verify(refreshTokenRepository).save(token);
    }

    private RefreshToken token(User user, OffsetDateTime expiresAt, OffsetDateTime revokedAt) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(revokedAt);
        return token;
    }

    private User activeUser() {
        User user = new User();
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