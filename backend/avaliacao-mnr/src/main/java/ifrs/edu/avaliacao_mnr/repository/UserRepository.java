package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(Role role);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
}