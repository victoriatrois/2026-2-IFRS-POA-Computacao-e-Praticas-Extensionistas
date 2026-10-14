package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> listAll() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Event not found: " + id
                ));
    }

    @Transactional
    public Event updateEvent(Long id, Event eventUpdated) {
        Event eventExistent = findById(id);

        eventExistent.setName(eventUpdated.getName());
        eventExistent.setDescription(eventUpdated.getDescription());
        eventExistent.setDate(eventUpdated.getDate());
        eventExistent.setStatus(eventUpdated.getStatus());

        return eventRepository.save(eventExistent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = findById(id);
        eventRepository.delete(event);
    }
}