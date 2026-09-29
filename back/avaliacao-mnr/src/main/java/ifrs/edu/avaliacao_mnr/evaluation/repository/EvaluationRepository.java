package ifrs.edu.avaliacao_mnr.evaluation.repository;

import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
}