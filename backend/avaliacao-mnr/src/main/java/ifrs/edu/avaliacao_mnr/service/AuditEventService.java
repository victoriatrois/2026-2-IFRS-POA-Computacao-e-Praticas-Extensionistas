package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.enums.AuditEventType;
import ifrs.edu.avaliacao_mnr.model.AuditEventEntity;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.AuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditEventService {

    private final AuditEventRepository repository;

    public AuditEventService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEventType eventType, User user, String email, boolean successful,
            String details, HttpServletRequest request) {
        recordActor(eventType, user == null ? null : user.getId(), email, successful, details, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordActor(AuditEventType eventType, UUID userId, String email, boolean successful,
            String details, HttpServletRequest request) {
        AuditEventEntity event = new AuditEventEntity();
        event.setUserId(userId);
        event.setEmail(limit(email, 255));
        event.setEventType(eventType.name());
        event.setSuccessful(successful);
        event.setDetails(limit(details, 512));
        if (request != null) {
            event.setIpAddress(limit(request.getRemoteAddr(), 64));
            event.setUserAgent(limit(request.getHeader("User-Agent"), 512));
        }
        repository.save(event);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}