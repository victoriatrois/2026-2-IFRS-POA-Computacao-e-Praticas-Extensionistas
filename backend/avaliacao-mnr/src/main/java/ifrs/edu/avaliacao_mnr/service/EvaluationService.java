
package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.EvaluationResponseDTO;
import ifrs.edu.avaliacao_mnr.dto.EvaluationDetailResponseDTO;
import ifrs.edu.avaliacao_mnr.dto.EvaluationScoreRequestDTO;
import ifrs.edu.avaliacao_mnr.dto.EvaluationScoreResponseDTO;
import ifrs.edu.avaliacao_mnr.model.Evaluation;
import ifrs.edu.avaliacao_mnr.model.EvaluationCriteria;
import ifrs.edu.avaliacao_mnr.model.EvaluationScore;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.EvaluationRepository;
import ifrs.edu.avaliacao_mnr.repository.ProjectEvaluatorAssignmentRepository;
import ifrs.edu.avaliacao_mnr.repository.EvaluationCriteriaRepository;
import ifrs.edu.avaliacao_mnr.repository.EvaluationScoreRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final ProjectEvaluatorAssignmentRepository assignmentRepository;
    private final EvaluationCriteriaRepository criteriaRepository;
    private final EvaluationScoreRepository scoreRepository;
    private final EvaluationCriteriaService criteriaService;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            UserRepository userRepository,
            ProjectEvaluatorAssignmentRepository assignmentRepository,
            EvaluationCriteriaRepository criteriaRepository,
            EvaluationScoreRepository scoreRepository,
            EvaluationCriteriaService criteriaService
    ) {
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.criteriaRepository = criteriaRepository;
        this.scoreRepository = scoreRepository;
        this.criteriaService = criteriaService;
    }

    @Transactional
    public EvaluationResponseDTO createEvaluation(
            Evaluation evaluation,
            UUID authenticatedUserId
    ) {
        if (evaluation.getAssignment() == null || evaluation.getAssignment().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An assignment is required to create an evaluation");
        }

        Long assignmentId = evaluation.getAssignment().getId();
        ProjectEvaluatorAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
        if (!assignment.getEvaluator().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You cannot evaluate an assignment of another evaluator");
        }
        if (assignment.getEvaluation() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An evaluation already exists for this assignment");
        }
        if (evaluation.getStatus() != ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new evaluation must start with IN_PROGRESS status");
        }

        evaluation.setId(null);
        evaluation.setAssignment(assignment);
        evaluation.setEvaluator(assignment.getEvaluator());
        evaluation.setCreatedAt(null);
        evaluation.setUpdatedAt(null);

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        return EvaluationResponseDTO.fromEntity(savedEvaluation);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponseDTO> listAll() {
        return evaluationRepository.findAll()
                .stream()
                .map(EvaluationResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EvaluationResponseDTO findById(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Evaluation not found: " + id));

        return EvaluationResponseDTO.fromEntity(evaluation);
    }

    @Transactional(readOnly = true)
    public EvaluationDetailResponseDTO findByAssignment(Long assignmentId, UUID authenticatedUserId) {
        Evaluation evaluation = evaluationRepository.findByAssignmentIdAndEvaluatorId(assignmentId, authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation not found"));
        return EvaluationDetailResponseDTO.fromEntity(evaluation);
    }

    @Transactional
    public EvaluationResponseDTO updateEvaluation(
            Long id,
            Evaluation evaluationUpdated,
            UUID authenticatedUserId
    ) {
        Evaluation existing = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Evaluation not found: " + id));

        if (!existing.getEvaluator().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException(
                    "You cannot update another evaluator's evaluation");
        }
        if (existing.getStatus() == ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Submitted evaluations cannot be changed");
        }

        if (evaluationUpdated.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evaluation status is required");
        }
        if (evaluationUpdated.getStatus() == ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.SUBMITTED) {
            copyFormAnswers(existing, evaluationUpdated);
            if (!Boolean.TRUE.equals(existing.getArticlePresent())
                    || !Boolean.TRUE.equals(existing.getVideoPresent())
                    || existing.getHonorableMention() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Both materials and the honorable mention answer are required before submission");
            }
            validateCompleteBeforeSubmission(existing);
        } else if (evaluationUpdated.getStatus()
                != ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid evaluation status transition");
        }

        if (evaluationUpdated.getStatus() == ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.IN_PROGRESS) {
            copyFormAnswers(existing, evaluationUpdated);
        }

        existing.setStatus(evaluationUpdated.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(existing);
        return EvaluationResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteEvaluation(Long id, UUID authenticatedUserId) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Evaluation not found: " + id));

        if (!evaluation.getEvaluator().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException(
                    "You cannot delete another evaluator's evaluation");
        }

        evaluationRepository.delete(evaluation);
    }

    @Transactional
    public EvaluationScoreResponseDTO saveScore(
            Long evaluationId,
            Long criterionId,
            EvaluationScoreRequestDTO request,
            UUID authenticatedUserId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + evaluationId));
        assertEvaluationOwner(evaluation, authenticatedUserId);
        if (evaluation.getStatus() == ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Submitted evaluations cannot be changed");
        }

        EvaluationCriteria criterion = criteriaRepository.findById(criterionId)
                .orElseThrow(() -> new EntityNotFoundException("Criterion not found: " + criterionId));
        if (evaluation.getAssignment() == null
                || !criterion.getEvent().getId().equals(evaluation.getAssignment().getProject().getEvent().getId())) {
            throw new IllegalArgumentException("Criterion does not belong to the evaluation event");
        }
        if (request.score() > criterion.getMaxScore()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must not exceed the criterion maximum");
        }

        EvaluationScore score = scoreRepository
                .findByEvaluationIdAndCriterionId(evaluationId, criterionId)
                .orElseGet(EvaluationScore::new);
        score.setEvaluation(evaluation);
        score.setCriterion(criterion);
        score.setScore(request.score().doubleValue());
        score.setComment(request.comment());
        if (evaluation.getStatus() == ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.IN_PROGRESS) {
            evaluation.setStatus(ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.IN_PROGRESS);
        }
        evaluation.setUpdatedAt(LocalDateTime.now());
        evaluationRepository.save(evaluation);
        return EvaluationScoreResponseDTO.fromEntity(scoreRepository.save(score));
    }

    private void assertEvaluationOwner(Evaluation evaluation, UUID authenticatedUserId) {
        if (!evaluation.getEvaluator().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You cannot change another evaluator's evaluation");
        }
    }

    private void validateCompleteBeforeSubmission(Evaluation evaluation) {
        if (evaluation.getAssignment() == null) {
            throw new IllegalStateException("Evaluation must have an assignment before submission");
        }

        Long eventId = evaluation.getAssignment().getProject().getEvent().getId();
        List<EvaluationCriteria> criteria = criteriaService.ensureDefaults(eventId);
        boolean incomplete = criteria.stream()
                .anyMatch(criterion -> !scoreRepository.existsByEvaluationIdAndCriterionId(
                        evaluation.getId(), criterion.getId()));
        if (incomplete) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All event criteria must be scored before submission");
        }
    }

    private void copyFormAnswers(Evaluation target, Evaluation source) {
        if (source.getArticlePresent() != null) target.setArticlePresent(source.getArticlePresent());
        if (source.getVideoPresent() != null) target.setVideoPresent(source.getVideoPresent());
        if (source.getHonorableMention() != null) target.setHonorableMention(source.getHonorableMention());
        if (source.getOrganizationNotes() != null) target.setOrganizationNotes(source.getOrganizationNotes());
        if (source.getParticipantFeedback() != null) target.setParticipantFeedback(source.getParticipantFeedback());
    }
}
