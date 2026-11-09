package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.EvaluationCriteriaResponseDTO;
import ifrs.edu.avaliacao_mnr.service.EvaluationCriteriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/evaluation-criteria")
public class EvaluationCriteriaController {

    private final EvaluationCriteriaService criteriaService;

    public EvaluationCriteriaController(EvaluationCriteriaService criteriaService) {
        this.criteriaService = criteriaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EVALUATION_READ')")
    public ResponseEntity<List<EvaluationCriteriaResponseDTO>> list(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(criteriaService.ensureDefaults(eventId)
                .stream()
                .map(EvaluationCriteriaResponseDTO::fromEntity)
                .toList());
    }
}
