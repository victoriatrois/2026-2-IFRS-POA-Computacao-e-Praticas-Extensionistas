package ifrs.edu.avaliacao_mnr.dto;

import java.util.List;

public record EvaluatorProjectListResponseDTO(
        List<EvaluatorProjectResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        EvaluatorProjectTotalsDTO totals
) {
}
