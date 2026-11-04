package ifrs.edu.avaliacao_mnr.dto;

public record EvaluatorProjectTotalsDTO(
        long assigned,
        long pending,
        long inProgress,
        long submitted
) {
}
