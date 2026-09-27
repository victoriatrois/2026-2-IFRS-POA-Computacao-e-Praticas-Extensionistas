package ifrs.edu.avaliacao_mnr.model;

public record ProjectValidationResult(
        boolean isPdfValid,
        boolean isVideoValid,
        boolean isValid
) {
    public boolean isMarkedForReview() {
        return !isValid;
    }
}
