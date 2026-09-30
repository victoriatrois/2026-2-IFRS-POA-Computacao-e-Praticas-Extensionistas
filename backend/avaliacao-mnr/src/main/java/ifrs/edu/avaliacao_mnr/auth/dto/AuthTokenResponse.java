package ifrs.edu.avaliacao_mnr.auth.dto;

import java.util.UUID;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        String refreshToken,
        UserProfile user
) {
    public record UserProfile(UUID id, String name, String surname, String email, String role) {
    }
}
