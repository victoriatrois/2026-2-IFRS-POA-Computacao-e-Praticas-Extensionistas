package ifrs.edu.avaliacao_mnr.service;

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
    public Evaluation createEvaluation(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public List<Evaluation> listAll() {
        return evaluationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Evaluation findById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Evaluation not found: " + id
                ));
    }

    @Transactional
    public Evaluation updateEvaluation(Long id, Evaluation evaluationUpdated) {
        Evaluation evaluationExistent = findById(id);

        evaluationExistent.setEvaluator(evaluationUpdated.getEvaluator());
        evaluationExistent.setStatus(evaluationUpdated.getStatus());

        return evaluationRepository.save(evaluationExistent);
    }

    @Transactional
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = findById(id);
        evaluationRepository.delete(evaluation);
    }
}