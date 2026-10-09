package ifrs.edu.avaliacao_mnr.project.repository;

import ifrs.edu.avaliacao_mnr.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByEventIdAndName(Long eventId, String name);

    Optional<Project> findByEventIdAndNameIgnoreCase(Long eventId, String name);

    List<Project> findByEventId(Long eventId);

    // (Issue #44): versão paginada para o endpoint
    // GET /api/events/{eventId}/projects.
    Page<Project> findByEventId(Long eventId, Pageable pageable);
}
