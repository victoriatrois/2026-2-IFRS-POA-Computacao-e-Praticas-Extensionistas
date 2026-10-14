package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event management")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EVENT_READ')")
    @Operation(
            summary = "List events",
            description = "Lists all registered events."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Event-read permission required")
    })
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_READ')")
    @Operation(
            summary = "Find event by ID",
            description = "Returns a registered event by its identifier."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Event-read permission required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EVENT_MANAGE')")
    @Operation(
            summary = "Create event",
            description = "Creates a new event."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Event-management permission required")
    })
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        return ResponseEntity.ok(eventService.createEvent(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_MANAGE')")
    @Operation(
            summary = "Update event",
            description = "Updates an existing event."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event updated"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Event-management permission required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody Event event
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, event));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_MANAGE')")
    @Operation(
            summary = "Delete event",
            description = "Deletes an existing event."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event deleted"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Event-management permission required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}