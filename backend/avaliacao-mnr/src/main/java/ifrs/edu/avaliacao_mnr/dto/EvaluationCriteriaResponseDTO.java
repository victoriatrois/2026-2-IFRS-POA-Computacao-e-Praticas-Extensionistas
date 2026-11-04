package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.model.EvaluationCriteria;

public record EvaluationCriteriaResponseDTO(
        Long id,
        Long eventId,
        String name,
        String description,
        Double maxScore,
        Double weight,
        Integer displayOrder
) {
    public static EvaluationCriteriaResponseDTO fromEntity(EvaluationCriteria criterion) {
        return new EvaluationCriteriaResponseDTO(
                criterion.getId(),
                criterion.getEvent().getId(),
                criterion.getName(),
                criterion.getDescription(),
                criterion.getMaxScore(),
                criterion.getWeight(),
                criterion.getOrder()
        );
    }
}
