package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.enums.EvaluationStatus;
import ifrs.edu.avaliacao_mnr.model.Evaluation;

import java.time.LocalDateTime;

public record EvaluationResponseDTO(
        Long id,
        java.util.UUID evaluatorId,
        String evaluatorName,
        EvaluationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EvaluationResponseDTO fromEntity(Evaluation evaluation) {
        return new EvaluationResponseDTO(
                evaluation.getId(),
                evaluation.getEvaluator().getId(),
                evaluation.getEvaluator().getName(),
                evaluation.getStatus(),
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt()
        );
    }
}
