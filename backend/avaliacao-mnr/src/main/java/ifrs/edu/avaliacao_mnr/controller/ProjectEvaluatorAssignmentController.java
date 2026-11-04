package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.ProjectEvaluatorAssignmentRequestDTO;
import ifrs.edu.avaliacao_mnr.dto.ProjectEvaluatorAssignmentResponseDTO;
import ifrs.edu.avaliacao_mnr.service.ProjectEvaluatorAssignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/{eventId}/project-evaluator-assignments")
@SecurityRequirement(name = "bearerAuth")
public class ProjectEvaluatorAssignmentController {

    private final ProjectEvaluatorAssignmentService assignmentService;

    public ProjectEvaluatorAssignmentController(ProjectEvaluatorAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_ASSIGN')")
    public ResponseEntity<ProjectEvaluatorAssignmentResponseDTO> assign(
            @PathVariable Long eventId,
            @Valid @RequestBody ProjectEvaluatorAssignmentRequestDTO request) {
        return ResponseEntity.ok(assignmentService.assignResponse(eventId, request));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAuthority('PROJECT_ASSIGN')")
    public ResponseEntity<Void> remove(@PathVariable Long assignmentId) {
        assignmentService.remove(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
