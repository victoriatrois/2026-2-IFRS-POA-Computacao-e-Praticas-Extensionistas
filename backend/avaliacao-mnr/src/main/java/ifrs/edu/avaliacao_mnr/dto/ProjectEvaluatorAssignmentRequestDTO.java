package ifrs.edu.avaliacao_mnr.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectEvaluatorAssignmentRequestDTO(
        @NotNull Long projectId,
        @NotNull UUID evaluatorId
) {
}
