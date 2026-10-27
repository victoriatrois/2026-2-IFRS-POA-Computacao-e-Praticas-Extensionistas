package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(Role role);

    @Query("""
        SELECT user FROM User user
        WHERE (:role IS NULL OR user.role = :role)
          AND (:active IS NULL OR user.active = :active)
          AND (:search IS NULL
           OR LOWER(user.name) LIKE LOWER(CONCAT(CONCAT('%', :search), '%'))
           OR LOWER(user.surname) LIKE LOWER(CONCAT(CONCAT('%', :search), '%'))
           OR LOWER(user.email) LIKE LOWER(CONCAT(CONCAT('%', :search), '%')))
        """)
    Page<User> searchUsers(@Param("role") Role role, @Param("active") Boolean active,
        @Param("search") String search, Pageable pageable);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
}