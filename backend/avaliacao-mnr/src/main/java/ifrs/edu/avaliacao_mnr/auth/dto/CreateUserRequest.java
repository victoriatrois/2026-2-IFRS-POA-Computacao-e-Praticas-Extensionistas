package ifrs.edu.avaliacao_mnr.auth.dto;

import ifrs.edu.avaliacao_mnr.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String surname,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 14) String cpf,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotNull Role role
) {
}
