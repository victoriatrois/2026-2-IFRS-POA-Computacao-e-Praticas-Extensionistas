package ifrs.edu.avaliacao_mnr.user.service;

import ifrs.edu.avaliacao_mnr.user.entity.User;
import ifrs.edu.avaliacao_mnr.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User update(Long id, User user) {
        User existingUser = findById(id);

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPasswordHash(user.getPasswordHash());
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        User existingUser = findById(id);
        userRepository.delete(existingUser);
    }
}