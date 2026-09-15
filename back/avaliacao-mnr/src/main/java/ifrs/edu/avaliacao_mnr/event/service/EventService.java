package ifrs.edu.avaliacao_mnr.event.service;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event create(Event event) {
        return eventRepository.save(event);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Event update(Long id, Event event) {
        Event existingEvent = findById(id);

        existingEvent.setName(event.getName());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setDate(event.getDate());
        existingEvent.setStatus(event.getStatus());
        existingEvent.setCreatedAt(event.getCreatedAt());

        return eventRepository.save(existingEvent);
    }

    public void delete(Long id) {
        Event existingEvent = findById(id);
        eventRepository.delete(existingEvent);
    }
}