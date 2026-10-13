
package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.EvaluationResponseDTO;
import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import ifrs.edu.avaliacao_mnr.repository.EvaluationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public EvaluationService(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public EvaluationResponseDTO createEvaluation(Evaluation evaluation) {
        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        return EvaluationResponseDTO.fromEntity(savedEvaluation);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponseDTO> listAll() {
        return ((Object) evaluationRepository.findAll()
                .stream()
                .map(EvaluationResponseDTO::fromEntity))
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
            Evaluation evaluationUpdated
    ) {
        Evaluation evaluationExistent = evaluationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Evaluation not found: " + id
                ));

        evaluationExistent.setEvaluator(evaluationUpdated.getEvaluator());
        evaluationExistent.setStatus(evaluationUpdated.getStatus());

        Evaluation savedEvaluation =
                evaluationRepository.save(evaluationExistent);

        return EvaluationResponseDTO.fromEntity(savedEvaluation);
    }

    @Transactional
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Evaluation not found: " + id
                ));

        evaluationRepository.delete(evaluation);
    }
}
