package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.enums.AuditEventType;
import ifrs.edu.avaliacao_mnr.model.AuditEventEntity;
import ifrs.edu.avaliacao_mnr.repository.AuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditEventServiceTest {

    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final AuditEventService auditService = new AuditEventService(repository);

    @ParameterizedTest
    @EnumSource(AuditEventType.class)
    void recordPersistsEachSupportedEventType(AuditEventType eventType) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("integration-test");

        auditService.record(eventType, null, "person@example.com", false, "test event", request);

        ArgumentCaptor<AuditEventEntity> eventCaptor = ArgumentCaptor.forClass(AuditEventEntity.class);
        verify(repository).save(eventCaptor.capture());
        AuditEventEntity event = eventCaptor.getValue();
        assertEquals(eventType.name(), event.getEventType());
        assertEquals("person@example.com", event.getEmail());
        assertEquals(false, event.isSuccessful());
        assertEquals("127.0.0.1", event.getIpAddress());
        assertEquals("integration-test", event.getUserAgent());
    }
}