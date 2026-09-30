package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.model.AuthAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEvent, UUID> {
}