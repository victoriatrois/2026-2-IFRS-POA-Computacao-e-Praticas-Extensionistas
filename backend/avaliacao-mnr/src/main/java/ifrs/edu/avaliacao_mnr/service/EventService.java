package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ifrs.edu.avaliacao_mnr.dto.EventResponseDTO;

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
    public List<EventResponseDTO> listAll() {
        return eventRepository.findAll()
                .stream()
                .map(EventResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponseDTO findById(Long id) {
        return EventResponseDTO.fromEntity(findEventById(id));
    }

    @Transactional
    public Event updateEvent(Long id, Event eventUpdated) {
        Event eventExistent = findEventById(id);
        eventExistent.setName(eventUpdated.getName());
        eventExistent.setDescription(eventUpdated.getDescription());
        eventExistent.setDate(eventUpdated.getDate());
        eventExistent.setStatus(eventUpdated.getStatus());
        return eventRepository.save(eventExistent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEventById(id);
        eventRepository.delete(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Event not found: " + id));
    }
}