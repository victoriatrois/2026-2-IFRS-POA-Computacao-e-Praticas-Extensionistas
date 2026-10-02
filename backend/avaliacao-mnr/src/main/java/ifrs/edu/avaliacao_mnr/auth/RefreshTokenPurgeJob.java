package ifrs.edu.avaliacao_mnr.auth;

import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

@Component
public class RefreshTokenPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenPurgeJob.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration retention;

    public RefreshTokenPurgeJob(RefreshTokenRepository refreshTokenRepository,
                                @Value("${app.security.jwt.refresh-token-purge.retention:P7D}") Duration retention) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.retention = retention;
    }

    @Scheduled(cron = "${app.security.jwt.refresh-token-purge.cron:0 30 3 * * *}")
    @Transactional
    public int purge() {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(retention);
        refreshTokenRepository.clearReplacementReferences(cutoff);
        int deleted = refreshTokenRepository.deleteExpiredOrRevokedBefore(cutoff);
        log.info("Purged {} expired/revoked refresh tokens older than {}", deleted, cutoff);
        return deleted;
    }
}
