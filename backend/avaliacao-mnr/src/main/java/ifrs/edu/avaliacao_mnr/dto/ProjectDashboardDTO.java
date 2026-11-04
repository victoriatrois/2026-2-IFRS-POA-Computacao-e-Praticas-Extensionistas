
package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.model.Project;
import java.time.OffsetDateTime;

public record ProjectDashboardDTO(
        Long id,
        String name,
        String level,
        String participantName,
        String institutionName,
        boolean markedForReview,
        boolean validated,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProjectDashboardDTO fromEntity(Project project) {
        return new ProjectDashboardDTO(
                project.getId(),
                project.getName(),
                project.getLevel(),
                project.getParticipantName(),
                project.getInstitutionName(),
                project.isMarkedForReview(),
                project.isValidated(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
