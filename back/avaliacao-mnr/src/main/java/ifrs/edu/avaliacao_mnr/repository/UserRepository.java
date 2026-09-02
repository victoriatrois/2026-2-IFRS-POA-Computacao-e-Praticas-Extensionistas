package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.model.User;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(Enum<?> role);
    boolean emailAlreadyUsed(String email);
}