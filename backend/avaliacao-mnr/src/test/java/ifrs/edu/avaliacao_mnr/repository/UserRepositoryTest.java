package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User newUser(String name, String email, String cpf, Role role) {
        User user = new User();
        user.setName(name);
        user.setSurname("Test");
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPasswordHash("password-hash");
        user.setRole(role);
        return user;
    }

    @Test
    void existsByEmailReflectsPersistedUsers() {
        userRepository.save(newUser("Bia", "bia@example.com", "00000000001", Role.ADMIN));

        assertTrue(userRepository.existsByEmail("bia@example.com"));
        assertFalse(userRepository.existsByEmail("nobody@example.com"));
    }

    @Test
    void findByRoleReturnsOnlyMatchingUsers() {
        userRepository.save(newUser("Bia", "bia@example.com", "00000000001", Role.ADMIN));
        userRepository.save(newUser("Alice", "alice@example.com", "00000000002", Role.EVALUATOR));
        userRepository.save(newUser("Maria", "maria@example.com", "00000000003", Role.EVALUATOR));

        List<String> evaluatorEmails = userRepository.findByRole(Role.EVALUATOR)
                .stream()
                .map(User::getEmail)
                .toList();

        assertEquals(2, evaluatorEmails.size());
        assertEquals(Set.of("maria@example.com", "alice@example.com"), new HashSet<>(evaluatorEmails));
    }

    @Test
    void emailMustBeUnique() {
        userRepository.save(newUser("Ana", "ana@example.com", "00000000001", Role.ADMIN));

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(newUser("Analu", "ana@example.com", "00000000002", Role.EVALUATOR)));
    }

    @Test
    void cpfMustBeUnique() {
        userRepository.save(newUser("Ana", "ana@example.com", "12345678900", Role.ADMIN));

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(newUser("Analu", "analu@example.com", "12345678900", Role.EVALUATOR)));
    }

    @Test
    void idIsGeneratedOnPersist() {
        User saved = userRepository.save(newUser("Ana", "ana@example.com", "00000000001", Role.ADMIN));

        assertNotNull(saved.getId());
    }
}