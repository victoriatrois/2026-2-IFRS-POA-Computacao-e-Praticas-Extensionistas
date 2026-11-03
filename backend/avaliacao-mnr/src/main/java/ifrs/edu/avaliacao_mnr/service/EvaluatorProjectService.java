package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.EvaluatorProjectListResponseDTO;
import ifrs.edu.avaliacao_mnr.dto.EvaluatorProjectResponseDTO;
import ifrs.edu.avaliacao_mnr.dto.EvaluatorProjectTotalsDTO;
import ifrs.edu.avaliacao_mnr.enums.EvaluatorProjectStatus;
import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;
import ifrs.edu.avaliacao_mnr.repository.EventRepository;
import ifrs.edu.avaliacao_mnr.repository.ProjectEvaluatorAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class EvaluatorProjectService {

    private final ProjectEvaluatorAssignmentRepository assignmentRepository;
    private final EventRepository eventRepository;

    public EvaluatorProjectService(
            ProjectEvaluatorAssignmentRepository assignmentRepository,
            EventRepository eventRepository) {
        this.assignmentRepository = assignmentRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public EvaluatorProjectListResponseDTO list(
            Long eventId,
            UUID evaluatorId,
            String search,
            String level,
            String status,
            Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        String normalizedLevel = level == null || level.isBlank() ? null : level.trim();
        String normalizedStatus = status == null || status.isBlank()
                ? null
                : parseStatus(status).name();

        Page<ProjectEvaluatorAssignment> page = assignmentRepository.findForEvaluator(
                eventId, evaluatorId, normalizedSearch, normalizedLevel, normalizedStatus, pageable);
        EvaluatorProjectTotalsDTO totals = totals(eventId, evaluatorId);

        return new EvaluatorProjectListResponseDTO(
                page.getContent().stream().map(EvaluatorProjectResponseDTO::fromEntity).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                totals
        );
    }

    private EvaluatorProjectTotalsDTO totals(Long eventId, UUID evaluatorId) {
        List<ProjectEvaluatorAssignment> assignments = assignmentRepository.findForEvaluator(
                eventId, evaluatorId, null, null, null, Pageable.unpaged()).getContent();
        long pending = assignments.stream().filter(a -> statusOf(a) == EvaluatorProjectStatus.PENDING).count();
        long inProgress = assignments.stream().filter(a -> statusOf(a) == EvaluatorProjectStatus.IN_PROGRESS).count();
        long submitted = assignments.stream().filter(a -> statusOf(a) == EvaluatorProjectStatus.SUBMITTED).count();
        return new EvaluatorProjectTotalsDTO(assignments.size(), pending, inProgress, submitted);
    }

    private EvaluatorProjectStatus statusOf(ProjectEvaluatorAssignment assignment) {
        return EvaluatorProjectResponseDTO.fromEntity(assignment).status();
    }

    private EvaluatorProjectStatus parseStatus(String value) {
        try {
            return EvaluatorProjectStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status. Expected PENDING, IN_PROGRESS or SUBMITTED",
                    exception);
        }
    }
}
