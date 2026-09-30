package ifrs.edu.avaliacao_mnr.config;

import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class BootstrapAdminConfig {

    @Bean
    ApplicationRunner bootstrapAdmin(
            UserRepository userRepository,
            UserService userService,
            @Value("${app.security.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${app.security.bootstrap-admin.name:}") String name,
            @Value("${app.security.bootstrap-admin.surname:}") String surname,
            @Value("${app.security.bootstrap-admin.email:}") String email,
            @Value("${app.security.bootstrap-admin.cpf:}") String cpf,
            @Value("${app.security.bootstrap-admin.password:}") String password
    ) {
        return args -> {
            if (!enabled) {
                return;
            }
            if (!StringUtils.hasText(name) || !StringUtils.hasText(surname) || !StringUtils.hasText(email)
                    || !StringUtils.hasText(cpf) || password.length() < 12) {
                throw new IllegalStateException("Bootstrap admin requires name, surname, email, cpf, and a password of at least 12 characters");
            }
            if (userRepository.findByEmailIgnoreCase(email.trim()).isPresent()) {
                return;
            }
            User admin = new User();
            admin.setName(name.trim());
            admin.setSurname(surname.trim());
            admin.setEmail(email.trim());
            admin.setCpf(cpf.trim());
            admin.setRole(Role.ADMIN);
            userService.createUser(admin, password);
        };
    }
}