package ifrs.edu.avaliacao_mnr.auth;

import com.jayway.jsonpath.JsonPath;
import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.AuthAuditEventRepository;
import ifrs.edu.avaliacao_mnr.repository.RefreshTokenRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    private static final String PASSWORD = "integration-password-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthAuditEventRepository auditEventRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        auditEventRepository.deleteAll();
        userService.createUser(user("Evaluator", "evaluator@example.com", Role.EVALUATOR, "123.456.789-00"), PASSWORD);
        userService.createUser(user("Administrator", "admin@example.com", Role.ADMIN, "123.456.789-01"), PASSWORD);
    }

    @Test
    void loginProtectedProfileRefreshRotationAndLogoutWorkEndToEnd() throws Exception {
        String accessToken = loginToken("evaluator@example.com", PASSWORD, "accessToken");
        String firstRefreshToken = loginToken("evaluator@example.com", PASSWORD, "refreshToken");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("evaluator@example.com"))
                .andExpect(jsonPath("$.role").value("EVALUATOR"));

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + firstRefreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String secondRefreshToken = JsonPath.read(refreshResult.getResponse().getContentAsString(), "$.refreshToken");
        assertNotEquals(firstRefreshToken, secondRefreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + firstRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + secondRefreshToken + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + secondRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(event -> event.getEventType().equals("LOGIN") && event.isSuccessful()));
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(event -> event.getEventType().equals("REFRESH") && event.isSuccessful()));
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(event -> event.getEventType().equals("LOGOUT") && event.isSuccessful()));
    }

    @Test
    void credentialsAndRoleRestrictionsAreEnforcedAndAudited() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"evaluator@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        String evaluatorToken = loginToken("evaluator@example.com", PASSWORD, "accessToken");
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isForbidden());
        User evaluator = userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals("evaluator@example.com"))
                .findFirst()
                .orElseThrow();

        String adminToken = loginToken("admin@example.com", PASSWORD, "accessToken");
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));

        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(event -> event.getEventType().equals("ACCESS_DENIED") && !event.isSuccessful()
                        && evaluator.getId().equals(event.getUserId())
                        && event.getEmail().equals("evaluator@example.com")));
        assertTrue(auditEventRepository.findAll().stream()
                .anyMatch(event -> event.getEventType().equals("LOGIN") && !event.isSuccessful()));
    }

        @Test
        void adminUserCreationValidatesCpfCheckDigits() throws Exception {
                String adminToken = loginToken("admin@example.com", PASSWORD, "accessToken");

                mockMvc.perform(post("/users")
                                                .header("Authorization", "Bearer " + adminToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"name":"Invalid","surname":"Cpf","email":"invalid-cpf@example.com","cpf":"123.456.789-00","password":"long-password-2026","role":"EVALUATOR"}
                                                                """))
                                .andExpect(status().isBadRequest());

                mockMvc.perform(post("/users")
                                                .header("Authorization", "Bearer " + adminToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"name":"Valid","surname":"Cpf","email":"valid-cpf@example.com","cpf":"529.982.247-25","password":"long-password-2026","role":"EVALUATOR"}
                                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.cpf").value("529.982.247-25"));
        }

        private String loginToken(String email, String password, String property) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
                return JsonPath.read(result.getResponse().getContentAsString(), "$." + property);
    }

    private User user(String name, String email, Role role, String cpf) {
        User user = new User();
        user.setName(name);
        user.setSurname("Test");
        user.setEmail(email);
        user.setCpf(cpf);
        user.setRole(role);
        return user;
    }
}
