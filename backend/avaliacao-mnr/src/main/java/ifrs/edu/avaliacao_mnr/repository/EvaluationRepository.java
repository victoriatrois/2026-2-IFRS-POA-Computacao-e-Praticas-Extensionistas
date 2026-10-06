package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.evaluation.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
}