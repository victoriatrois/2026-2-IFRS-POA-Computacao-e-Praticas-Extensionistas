package ifrs.edu.avaliacao_mnr.dto;

import ifrs.edu.avaliacao_mnr.event.entity.Event;

public record EventResponseDTO(
        Long id,
        String name,
        String description,
        String date,
        String status
) {
    public static EventResponseDTO fromEntity(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getDate() != null ? event.getDate().toString() : null,
                event.getStatus() != null ? event.getStatus().name() : null
        );
    }
}
