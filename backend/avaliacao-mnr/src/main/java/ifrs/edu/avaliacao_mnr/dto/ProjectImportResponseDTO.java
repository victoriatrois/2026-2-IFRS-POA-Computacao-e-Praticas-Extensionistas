package ifrs.edu.avaliacao_mnr.dto;

import java.util.List;

public record ProjectImportResponseDTO(
        Long eventId,
        String eventName,
        int totalProcessed,
        int totalCreated,
        int totalUpdated,
        int totalMarkedForReview,
        List<ProjectResponseDTO> projects
) {}
