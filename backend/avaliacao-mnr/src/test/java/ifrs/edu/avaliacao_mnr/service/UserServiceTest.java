package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User sampleUser() {
        User user = new User();
        user.setName("Maria");
        user.setSurname("Silva");
        user.setEmail("maria@example.com");
        user.setCpf("123.456.789-00");
        user.setRole(Role.EVALUATOR);
        return user;
    }

    @Test
    void createUserStoresPasswordAsBcryptHash() {
        User created = userService.createUser(sampleUser(), "passw0rd!");

        assertNotEquals("passw0rd!", created.getPasswordHash());
        assertTrue(created.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches("passw0rd!", created.getPasswordHash()));
        verify(userRepository).save(created);
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.createUser(sampleUser(), "passw0rd!"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserNormalizesEmailBeforeCheckingAndSaving() {
        User user = sampleUser();
        user.setEmail("  Maria@Example.COM ");

        User created = userService.createUser(user, "passw0rd!");

        assertEquals("maria@example.com", created.getEmail());
        verify(userRepository).existsByEmailIgnoreCase("maria@example.com");
    }

    @Test
    void createUserRejectsDuplicateEmailWithDifferentCase() {
        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);
        User user = sampleUser();
        user.setEmail("MARIA@example.com");

        assertThrows(RuntimeException.class, () -> userService.createUser(user, "passw0rd!"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserNormalizesEmailAndRejectsCaseInsensitiveDuplicate() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(sampleUser()));
        when(userRepository.existsByEmailIgnoreCase("other@example.com")).thenReturn(true);
        User update = sampleUser();
        update.setEmail("Other@Example.com");

        assertThrows(RuntimeException.class, () -> userService.updateUser(id, update));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAllowsSameEmailWithDifferentCase() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(sampleUser()));
        User update = sampleUser();
        update.setEmail("MARIA@example.com");

        User saved = userService.updateUser(id, update);

        assertEquals("maria@example.com", saved.getEmail());
        verify(userRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    void findByIdReturnsUserWhenPresent() {
        UUID id = UUID.randomUUID();
        User user = sampleUser();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertSame(user, userService.findById(id));
    }

    @Test
    void findByIdThrowsWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findById(id));
    }
}