package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.EvaluationCriteriaResponseDTO;
import ifrs.edu.avaliacao_mnr.repository.EvaluationCriteriaRepository;
import ifrs.edu.avaliacao_mnr.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/evaluation-criteria")
public class EvaluationCriteriaController {

    private final EvaluationCriteriaRepository criteriaRepository;
    private final EventRepository eventRepository;

    public EvaluationCriteriaController(
            EvaluationCriteriaRepository criteriaRepository,
            EventRepository eventRepository) {
        this.criteriaRepository = criteriaRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EVALUATION_READ')")
    public ResponseEntity<List<EvaluationCriteriaResponseDTO>> list(
            @PathVariable Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found: " + eventId);
        }
        return ResponseEntity.ok(criteriaRepository.findByEventIdOrderByOrderAsc(eventId)
                .stream()
                .map(EvaluationCriteriaResponseDTO::fromEntity)
                .toList());
    }
}
