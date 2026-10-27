package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.enums.EventStatus;
import ifrs.edu.avaliacao_mnr.model.Event;

import java.time.LocalDate;

public record EventResponseDTO(
        Long id,
        String name,
        String description,
        LocalDate date,
        EventStatus status) {
    public static EventResponseDTO fromEntity(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getDate(),
                event.getStatus());
    }
}
