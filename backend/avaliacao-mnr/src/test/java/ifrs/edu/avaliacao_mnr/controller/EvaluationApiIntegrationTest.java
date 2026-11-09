package ifrs.edu.avaliacao_mnr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifrs.edu.avaliacao_mnr.enums.EventStatus;
import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.Event;
import ifrs.edu.avaliacao_mnr.model.Project;
import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.EvaluationCriteriaRepository;
import ifrs.edu.avaliacao_mnr.repository.EvaluationRepository;
import ifrs.edu.avaliacao_mnr.repository.EvaluationScoreRepository;
import ifrs.edu.avaliacao_mnr.repository.AuditEventRepository;
import ifrs.edu.avaliacao_mnr.repository.EventRepository;
import ifrs.edu.avaliacao_mnr.repository.ProjectEvaluatorAssignmentRepository;
import ifrs.edu.avaliacao_mnr.repository.ProjectRepository;
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

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvaluationApiIntegrationTest {

    private static final String PASSWORD = "integration-password-2026";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private MockMvc mockMvc;
    @Autowired private EventRepository eventRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectEvaluatorAssignmentRepository assignmentRepository;
    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private EvaluationScoreRepository scoreRepository;
    @Autowired private EvaluationCriteriaRepository criteriaRepository;
    @Autowired private UserService userService;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private AuditEventRepository auditEventRepository;

    private Event event;
    private Project project;
    private User admin;
    private User evaluator;
    private User otherEvaluator;

    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        evaluationRepository.deleteAll();
        assignmentRepository.deleteAll();
        criteriaRepository.deleteAll();
        projectRepository.deleteAll();
        eventRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        auditEventRepository.deleteAll();
        userRepository.deleteAll();

        event = new Event();
        event.setName("Evaluation integration test");
        event.setDate(LocalDate.of(2026, 11, 3));
        event.setStatus(EventStatus.OPEN);
        event = eventRepository.save(event);

        project = new Project();
        project.setEvent(event);
        project.setName("Assigned project");
        project = projectRepository.save(project);

        admin = saveUser("Admin", "admin-test@example.com", "11111111111", Role.ADMIN);
        evaluator = saveUser("Evaluator", "evaluator-test@example.com", "22222222222", Role.EVALUATOR);
        otherEvaluator = saveUser("Other", "other-test@example.com", "33333333333", Role.EVALUATOR);
    }

    @Test
    void adminAssignmentIsIsolatedAndEvaluationCanOnlyBeSubmittedOnce() throws Exception {
        String adminToken = loginToken(admin.getEmail());
        String evaluatorToken = loginToken(evaluator.getEmail());
        String otherEvaluatorToken = loginToken(otherEvaluator.getEmail());

        mockMvc.perform(post("/api/events/{eventId}/project-evaluator-assignments", event.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "projectId", project.getId(), "evaluatorId", evaluator.getId()))))
                .andExpect(status().isOk());

        ProjectEvaluatorAssignment assignment = assignmentRepository
                .findByProjectIdAndEvaluatorId(project.getId(), evaluator.getId()).orElseThrow();

        mockMvc.perform(get("/api/events/{eventId}/evaluator/projects", event.getId())
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].project.id").value(project.getId()));

        mockMvc.perform(get("/api/events/{eventId}/evaluator/projects", event.getId())
                        .header("Authorization", "Bearer " + otherEvaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/events/{eventId}/evaluation-criteria", event.getId())
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$.length()").value(13));

        String createResponse = mockMvc.perform(post("/api/evaluations")
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "assignment", Map.of("id", assignment.getId()),
                                "status", "IN_PROGRESS",
                                "articlePresent", true,
                                "videoPresent", true,
                                "honorableMention", false,
                                "organizationNotes", "Private organization note",
                                "participantFeedback", "Feedback for the team"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long evaluationId = objectMapper.readTree(createResponse).get("id").asLong();

        for (var criterion : criteriaRepository.findByEventIdOrderByOrderAsc(event.getId())) {
            mockMvc.perform(put("/api/evaluations/{evaluationId}/scores/{criterionId}", evaluationId, criterion.getId())
                            .header("Authorization", "Bearer " + evaluatorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\":4}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(put("/api/evaluations/{id}", evaluationId)
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUBMITTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mockMvc.perform(put("/api/evaluations/{id}", evaluationId)
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/evaluations/by-assignment/{assignmentId}", assignment.getId())
                        .header("Authorization", "Bearer " + evaluatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.articlePresent").value(true))
                .andExpect(jsonPath("$.honorableMention").value(false))
                .andExpect(jsonPath("$.organizationNotes").value("Private organization note"))
                .andExpect(jsonPath("$.participantFeedback").value("Feedback for the team"))
                .andExpect(jsonPath("$.scores.length()").value(13));

        mockMvc.perform(get("/api/evaluations/by-assignment/{assignmentId}", assignment.getId())
                        .header("Authorization", "Bearer " + otherEvaluatorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void evaluatorCannotAssignProjects() throws Exception {
        String evaluatorToken = loginToken(evaluator.getEmail());
        mockMvc.perform(post("/api/events/{eventId}/project-evaluator-assignments", event.getId())
                        .header("Authorization", "Bearer " + evaluatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "projectId", project.getId(), "evaluatorId", evaluator.getId()))))
                .andExpect(status().isForbidden());
    }

    private User saveUser(String name, String email, String cpf, Role role) {
        User user = new User();
        user.setName(name);
        user.setSurname("Test");
        user.setEmail(email);
        user.setCpf(cpf);
        user.setRole(role);
        return userService.createUser(user, PASSWORD);
    }

    private String loginToken(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
