package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.EventResponseDTO;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Endpoints de gerenciamento e consulta de eventos")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EVENT_READ')")
    @Operation(summary = "List all events", description = "Returns all available events.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<EventResponseDTO>> listEvents() {
        List<EventResponseDTO> events = eventRepository.findAll().stream()
                .map(EventResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(events);
    }
}
