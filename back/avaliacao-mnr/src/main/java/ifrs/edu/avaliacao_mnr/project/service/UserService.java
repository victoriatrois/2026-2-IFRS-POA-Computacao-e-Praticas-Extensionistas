package ifrs.edu.avaliacao_mnr.project.service;

import org.springframework.stereotype.Service;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (userRepository.emailAlreadyUsed(user.getEmail())) {
            throw new RuntimeException("E-mail already used in the system.");
        }

        return userRepository.save(user);
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.getReferenceById(id);
    }

    public User updateUser(Long id, User userUpdated) {
        User userExistent = findById(id);

        try { 
            
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        userExistent.setName(userUpdated.getName());
        userExistent.setEmail(userUpdated.getEmail());
        
        return userRepository.save(userExistent);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("It's not possible to deleter. User not found.");
        }
        userRepository.deleteById(id);
    }
}
