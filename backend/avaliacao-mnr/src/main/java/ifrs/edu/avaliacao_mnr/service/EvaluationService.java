
package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.EvaluationResponseDTO;
import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.EvaluationRepository;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            UserRepository userRepository
    ) {
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EvaluationResponseDTO createEvaluation(
            Evaluation evaluation,
            UUID authenticatedUserId
    ) {
        User evaluator = userRepository.findById(authenticatedUserId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Authenticated user not found"));

        // The evaluator must be the authenticated user, not a user
        // supplied in the request body.
        evaluation.setEvaluator(evaluator);

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

        // Do not replace the evaluator using data from the request.
        existing.setStatus(evaluationUpdated.getStatus());

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
}
