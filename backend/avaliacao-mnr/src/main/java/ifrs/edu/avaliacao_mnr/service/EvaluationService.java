
package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.EvaluationResponseDTO;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.repository.UserRepository;
import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import ifrs.edu.avaliacao_mnr.repository.EvaluationRepository;
import jakarta.persistence.EntityNotFoundException;
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
        UserRepository userRepository) {
    this.evaluationRepository = evaluationRepository;
    this.userRepository = userRepository;
}

    
    @Transactional
    public EvaluationResponseDTO createEvaluation(
        Evaluation evaluation,
        UUID authenticatedUserId) {

    User evaluator = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new EntityNotFoundException(
                    "Authenticated user not found"));

    // Ignora qualquer avaliador enviado no corpo da requisição.
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
                .orElseThrow(() -> new EntityNotFoundException(
                        "Evaluation not found: " + id
                ));

        return EvaluationResponseDTO.fromEntity(evaluation);
    }

    
    @Transactional
    public EvaluationResponseDTO updateEvaluation(
            Long id,
            Evaluation evaluationUpdated,
            UUID authenticatedUserId) {

        Evaluation existing = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Evaluation not found: " + id));

        if (!existing.getEvaluator().getId().equals(authenticatedUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot update another evaluator's evaluation");
        }

        // Não substitua o avaliador pelo valor enviado na requisição.
        existing.setStatus(evaluationUpdated.getStatus());

        Evaluation saved = evaluationRepository.save(existing);
        return EvaluationResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Evaluation not found: " + id));

        if (!evaluation.getEvaluator().getId().equals(authenticatedUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot delete another evaluator's evaluation");
        }

        evaluationRepository.delete(evaluation);
    }

}
