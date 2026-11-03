package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.enums.EvaluatorProjectStatus;
import ifrs.edu.avaliacao_mnr.enums.EvaluationStatus;
import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;

import java.time.LocalDateTime;

public record EvaluatorProjectResponseDTO(
        ProjectResponseDTO project,
        EvaluatorProjectStatus status,
        LocalDateTime assignedAt
) {
    public static EvaluatorProjectResponseDTO fromEntity(ProjectEvaluatorAssignment assignment) {
        EvaluationStatus evaluationStatus = assignment.getEvaluation() == null
                ? null
                : assignment.getEvaluation().getStatus();
        EvaluatorProjectStatus status = evaluationStatus == null
                ? EvaluatorProjectStatus.PENDING
                : evaluationStatus == EvaluationStatus.SUBMITTED
                        ? EvaluatorProjectStatus.SUBMITTED
                        : EvaluatorProjectStatus.IN_PROGRESS;
        return new EvaluatorProjectResponseDTO(
                ProjectResponseDTO.fromEntity(assignment.getProject()),
                status,
                assignment.getAssignedAt()
        );
    }
}
