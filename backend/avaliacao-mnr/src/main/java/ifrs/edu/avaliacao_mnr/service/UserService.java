package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (userRepository.existsByEmail(user.getEmail())) {
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

        if (!userExistent.getEmail().equals(userUpdated.getEmail())
                && userRepository.existsByEmail(userUpdated.getEmail())) {
            throw new RuntimeException("E-mail already used in the system.");
        }

        userExistent.setName(userUpdated.getName());
        userExistent.setEmail(userUpdated.getEmail());
        return userRepository.save(userExistent);
    }

    public void deleteUser(UUID id) {
        userRepository.delete(findById(id));
    }
}