package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.model.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {
    List<EvaluationCriteria> findByEventIdOrderByOrderAsc(Long eventId);
}
