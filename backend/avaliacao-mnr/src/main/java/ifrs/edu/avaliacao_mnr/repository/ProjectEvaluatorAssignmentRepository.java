package ifrs.edu.avaliacao_mnr.repository;

import ifrs.edu.avaliacao_mnr.model.ProjectEvaluatorAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;

public interface ProjectEvaluatorAssignmentRepository
        extends JpaRepository<ProjectEvaluatorAssignment, Long> {

    Optional<ProjectEvaluatorAssignment> findByProjectIdAndEvaluatorId(Long projectId, UUID evaluatorId);

    @EntityGraph(attributePaths = {"project", "evaluation"})
    @Query("""
            select a
            from ProjectEvaluatorAssignment a
            left join a.evaluation e
            where a.evaluator.id = :evaluatorId
              and a.project.event.id = :eventId
              and (:search is null or lower(a.project.name) like lower(concat('%', :search, '%')))
              and (:level is null or a.project.level = :level)
              and (
                  :status is null
                  or (:status = 'PENDING' and e is null)
                  or (:status = 'IN_PROGRESS' and e.status = ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.IN_PROGRESS)
                  or (:status = 'SUBMITTED' and e.status = ifrs.edu.avaliacao_mnr.enums.EvaluationStatus.SUBMITTED)
              )
            """)
    Page<ProjectEvaluatorAssignment> findForEvaluator(
            @Param("eventId") Long eventId,
            @Param("evaluatorId") UUID evaluatorId,
            @Param("search") String search,
            @Param("level") String level,
            @Param("status") String status,
            Pageable pageable);
}
