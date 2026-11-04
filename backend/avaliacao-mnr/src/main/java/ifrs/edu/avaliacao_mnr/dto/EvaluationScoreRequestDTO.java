package ifrs.edu.avaliacao_mnr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvaluationScoreRequestDTO(
        @NotNull @Min(1) @Max(5) Integer score,
        String comment
) {
}
