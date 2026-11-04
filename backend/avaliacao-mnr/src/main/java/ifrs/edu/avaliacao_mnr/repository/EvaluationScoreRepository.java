package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.model.EvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Long> {
    Optional<EvaluationScore> findByEvaluationIdAndCriterionId(Long evaluationId, Long criterionId);

    boolean existsByEvaluationIdAndCriterionId(Long evaluationId, Long criterionId);
}
