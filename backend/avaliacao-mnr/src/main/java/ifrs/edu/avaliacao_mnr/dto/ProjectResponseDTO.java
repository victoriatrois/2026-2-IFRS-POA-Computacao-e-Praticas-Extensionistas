package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.model.Project;
import java.time.OffsetDateTime;

public record ProjectResponseDTO(
        Long id,
        Long eventId,
        String name,
        String pdfUrl,
        String level,
        String videoUrl,
        String participantName,
        String participantCpf,
        String participantEmail,
        String institutionName,
        boolean markedForReview,
        boolean validated,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProjectResponseDTO fromEntity(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getEvent() != null ? project.getEvent().getId() : null,
                project.getName(),
                project.getPdfUrl(),
                project.getLevel(),
                project.getVideoUrl(),
                project.getParticipantName(),
                project.getParticipantCpf(),
                project.getParticipantEmail(),
                project.getInstitutionName(),
                project.isMarkedForReview(),
                project.isValidated(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
