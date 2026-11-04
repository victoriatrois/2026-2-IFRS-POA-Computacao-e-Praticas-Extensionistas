package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.ProjectEvaluatorAssignmentRequestDTO;
import ifrs.edu.avaliacao_mnr.dto.ProjectEvaluatorAssignmentResponseDTO;
import ifrs.edu.avaliacao_mnr.model.Event;
import ifrs.edu.avaliacao_mnr.model.Project;
import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.EventRepository;
import ifrs.edu.avaliacao_mnr.repository.ProjectEvaluatorAssignmentRepository;
import ifrs.edu.avaliacao_mnr.repository.ProjectRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProjectEvaluatorAssignmentService {

    private final ProjectEvaluatorAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ProjectEvaluatorAssignmentService(
            ProjectEvaluatorAssignmentRepository assignmentRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            EventRepository eventRepository) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public ProjectEvaluatorAssignment assign(
            Long eventId, ProjectEvaluatorAssignmentRequestDTO request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.projectId()));
        if (!project.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException("Project does not belong to event");
        }

        User evaluator = userRepository.findById(request.evaluatorId())
                .orElseThrow(() -> new EntityNotFoundException("Evaluator not found: " + request.evaluatorId()));
        if (evaluator.getRole() != ifrs.edu.avaliacao_mnr.enums.Role.EVALUATOR) {
            throw new IllegalArgumentException("User is not an evaluator");
        }
        return assignmentRepository.findByProjectIdAndEvaluatorId(project.getId(), evaluator.getId())
                .orElseGet(() -> {
                    ProjectEvaluatorAssignment assignment = new ProjectEvaluatorAssignment();
                    assignment.setProject(project);
                    assignment.setEvaluator(evaluator);
                    return assignmentRepository.save(assignment);
                });
    }

    @Transactional
    public ProjectEvaluatorAssignmentResponseDTO assignResponse(
            Long eventId, ProjectEvaluatorAssignmentRequestDTO request) {
        return ProjectEvaluatorAssignmentResponseDTO.fromEntity(assign(eventId, request));
    }

    @Transactional
    public void remove(Long assignmentId) {
        ProjectEvaluatorAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
        if (assignment.getEvaluation() != null) {
            throw new AccessDeniedException("Assignment cannot be removed after evaluation has started");
        }
        assignmentRepository.delete(assignment);
    }
}
