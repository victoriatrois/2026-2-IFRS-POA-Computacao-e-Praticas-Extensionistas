package ifrs.edu.avaliacao_mnr.auth;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.RefreshToken;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RefreshTokenPurgeJobTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private RefreshTokenPurgeJob job;
    private User user;

    @BeforeEach
    void setUp() {
        job = new RefreshTokenPurgeJob(refreshTokenRepository, Duration.ofDays(7));
        user = new User();
        user.setName("Bia");
        user.setSurname("Test");
        user.setEmail("bia@example.com");
        user.setCpf("00000000001");
        user.setPasswordHash("password-hash");
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);
    }

    private RefreshToken token(String hash, OffsetDateTime expiresAt, OffsetDateTime revokedAt) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(revokedAt);
        return refreshTokenRepository.saveAndFlush(token);
    }

    @Test
    void deletesTokensExpiredBeyondRetention() {
        OffsetDateTime now = OffsetDateTime.now();
        token("old-expired", now.minusDays(8), null);

        assertEquals(1, job.purge());
        assertTrue(refreshTokenRepository.findByTokenHash("old-expired").isEmpty());
    }

    @Test
    void deletesTokensRevokedBeyondRetention() {
        OffsetDateTime now = OffsetDateTime.now();
        token("old-revoked", now.plusDays(20), now.minusDays(8));

        assertEquals(1, job.purge());
        assertTrue(refreshTokenRepository.findByTokenHash("old-revoked").isEmpty());
    }

    @Test
    void keepsActiveAndRecentlyExpiredOrRevokedTokens() {
        OffsetDateTime now = OffsetDateTime.now();
        token("active", now.plusDays(10), null);
        token("recently-expired", now.minusDays(1), null);
        token("recently-revoked", now.plusDays(10), now.minusDays(1));

        assertEquals(0, job.purge());
        assertEquals(3, refreshTokenRepository.count());
    }

    @Test
    void clearsReplacementReferencesToPurgedTokens() {
        OffsetDateTime now = OffsetDateTime.now();
        RefreshToken purged = token("purged", now.minusDays(9), null);
        RefreshToken kept = token("kept", now.plusDays(10), now.minusDays(1));
        kept.setReplacedByTokenId(purged.getId());
        refreshTokenRepository.saveAndFlush(kept);
        RefreshToken unrelated = token("unrelated", now.plusDays(10), now.minusDays(1));
        UUID otherId = UUID.randomUUID();
        unrelated.setReplacedByTokenId(otherId);
        refreshTokenRepository.saveAndFlush(unrelated);

        assertEquals(1, job.purge());

        assertNull(refreshTokenRepository.findByTokenHash("kept").orElseThrow().getReplacedByTokenId());
        assertEquals(otherId,
                refreshTokenRepository.findByTokenHash("unrelated").orElseThrow().getReplacedByTokenId());
    }
}
