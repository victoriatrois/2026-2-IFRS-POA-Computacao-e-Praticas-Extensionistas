package ifrs.edu.avaliacao_mnr.auth.dto;

import ifrs.edu.avaliacao_mnr.model.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(UUID id, String name, String surname, String email, String cpf,
                           String role, boolean active, OffsetDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getSurname(), user.getEmail(),
                user.getCpf(), user.getRole().name(), user.isActive(), user.getCreatedAt());
    }
}