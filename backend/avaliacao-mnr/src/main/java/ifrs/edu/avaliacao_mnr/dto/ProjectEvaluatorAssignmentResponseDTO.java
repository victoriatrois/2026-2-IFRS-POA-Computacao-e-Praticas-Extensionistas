package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectEvaluatorAssignmentResponseDTO(
        Long id,
        Long projectId,
        UUID evaluatorId,
        LocalDateTime assignedAt
) {
    public static ProjectEvaluatorAssignmentResponseDTO fromEntity(ProjectEvaluatorAssignment assignment) {
        return new ProjectEvaluatorAssignmentResponseDTO(
                assignment.getId(),
                assignment.getProject().getId(),
                assignment.getEvaluator().getId(),
                assignment.getAssignedAt()
        );
    }
}
