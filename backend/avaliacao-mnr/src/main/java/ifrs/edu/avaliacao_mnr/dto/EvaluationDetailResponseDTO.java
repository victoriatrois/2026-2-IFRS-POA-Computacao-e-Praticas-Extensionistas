package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.model.Evaluation;

import java.util.List;

public record EvaluationDetailResponseDTO(
        Long id,
        Long assignmentId,
        String status,
        Boolean articlePresent,
        Boolean videoPresent,
        Boolean honorableMention,
        String organizationNotes,
        String participantFeedback,
        List<Score> scores
) {
    public record Score(Long criterionId, Integer value) { }

    public static EvaluationDetailResponseDTO fromEntity(Evaluation evaluation) {
        return new EvaluationDetailResponseDTO(
                evaluation.getId(), evaluation.getAssignment().getId(), evaluation.getStatus().name(),
                evaluation.getArticlePresent(), evaluation.getVideoPresent(), evaluation.getHonorableMention(),
                evaluation.getOrganizationNotes(), evaluation.getParticipantFeedback(),
                evaluation.getScores().stream()
                        .map(score -> new Score(score.getCriterion().getId(), score.getScore().intValue()))
                        .toList());
    }
}
