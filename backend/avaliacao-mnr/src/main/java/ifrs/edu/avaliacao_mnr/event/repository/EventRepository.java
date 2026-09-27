package ifrs.edu.avaliacao_mnr.event.repository;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByName(String name);
    Optional<Event> findByNameIgnoreCase(String name);
}
