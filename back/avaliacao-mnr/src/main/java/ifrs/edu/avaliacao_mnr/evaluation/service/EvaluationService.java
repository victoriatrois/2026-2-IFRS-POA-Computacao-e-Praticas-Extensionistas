package ifrs.edu.avaliacao_mnr.evaluation.service;

import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import ifrs.edu.avaliacao_mnr.evaluation.repository.EvaluationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public EvaluationService(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    public Evaluation create(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }

    public Evaluation findById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
    }

    public List<Evaluation> findAll() {
        return evaluationRepository.findAll();
    }

    public Evaluation update(Long id, Evaluation evaluation) {
        Evaluation existingEvaluation = findById(id);

        existingEvaluation.setEvaluator(evaluation.getEvaluator());
        existingEvaluation.setStatus(evaluation.getStatus());
        existingEvaluation.setCreatedAt(evaluation.getCreatedAt());
        existingEvaluation.setUpdatedAt(evaluation.getUpdatedAt());

        return evaluationRepository.save(existingEvaluation);
    }

    public void delete(Long id) {
        Evaluation existingEvaluation = findById(id);
        evaluationRepository.delete(existingEvaluation);
    }
}