package ifrs.edu.avaliacao_mnr.event.repository;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}