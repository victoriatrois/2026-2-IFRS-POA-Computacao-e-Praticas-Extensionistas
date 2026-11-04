package ifrs.edu.avaliacao_mnr.dto;

import jakarta.validation.constraints.NotNull;

public record EvaluationScoreRequestDTO(
        @NotNull Double score,
        String comment
) {
}
