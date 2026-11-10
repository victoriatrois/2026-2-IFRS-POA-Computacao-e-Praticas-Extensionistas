package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.model.EvaluationScore;

public record EvaluationScoreResponseDTO(
        Long id,
        Long evaluationId,
        Long criterionId,
        Double score,
        String comment
) {
    public static EvaluationScoreResponseDTO fromEntity(EvaluationScore score) {
        return new EvaluationScoreResponseDTO(
                score.getId(),
                score.getEvaluation().getId(),
                score.getCriterion().getId(),
                score.getScore(),
                score.getComment()
        );
    }
}
