package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user, String rawPassword) {
        user.setEmail(normalizeEmail(user.getEmail()));
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new RuntimeException("E-mail already used in the system.");
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    public User updateUser(UUID id, User userUpdated) {
        User userExistent = findById(id);

        String newEmail = normalizeEmail(userUpdated.getEmail());
        if (!userExistent.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new RuntimeException("E-mail already used in the system.");
        }

        userExistent.setName(userUpdated.getName());
        userExistent.setEmail(newEmail);
        return userRepository.save(userExistent);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void deleteUser(UUID id) {
        userRepository.delete(findById(id));
    }
}