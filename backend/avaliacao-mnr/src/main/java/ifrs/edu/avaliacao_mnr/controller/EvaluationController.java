
package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.EvaluationResponseDTO;
import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import ifrs.edu.avaliacao_mnr.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import ifrs.edu.avaliacao_mnr.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Evaluations", description = "Evaluation management")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EVALUATION_READ')")
    @Operation(
            summary = "List evaluations",
            description = "Lists all registered evaluations."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluations returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Evaluation-read permission required")
    })
    public ResponseEntity<List<EvaluationResponseDTO>> getAllEvaluations() {
        return ResponseEntity.ok(evaluationService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EVALUATION_READ')")
    @Operation(
            summary = "Find evaluation by ID",
            description = "Returns a registered evaluation by its identifier."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Evaluation-read permission required"),
            @ApiResponse(responseCode = "404", description = "Evaluation not found")
    })
    public ResponseEntity<EvaluationResponseDTO> getEvaluationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(evaluationService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EVALUATION_WRITE')")
    @Operation(
            summary = "Create evaluation",
            description = "Creates a new evaluation."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation created"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Evaluation-write permission required")
    })
    
    public ResponseEntity<EvaluationResponseDTO> createEvaluation(
        @RequestBody Evaluation evaluation,
        @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

    return ResponseEntity.ok(
            evaluationService.createEvaluation(
                    evaluation,
                    authenticatedUser.getId()
            )
    );
}

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EVALUATION_WRITE')")
    @Operation(
            summary = "Update evaluation",
            description = "Updates an existing evaluation."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation updated"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Evaluation-write permission required"),
            @ApiResponse(responseCode = "404", description = "Evaluation not found")
    })
    public ResponseEntity<EvaluationResponseDTO> updateEvaluation(
            @PathVariable Long id,
            @RequestBody Evaluation evaluation
    ) {
        return ResponseEntity.ok(
                evaluationService.updateEvaluation(id, evaluation, null)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EVALUATION_WRITE')")
    @Operation(
            summary = "Delete evaluation",
            description = "Deletes an existing evaluation."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evaluation deleted"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Evaluation-write permission required"),
            @ApiResponse(responseCode = "404", description = "Evaluation not found")
    })
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }
}
