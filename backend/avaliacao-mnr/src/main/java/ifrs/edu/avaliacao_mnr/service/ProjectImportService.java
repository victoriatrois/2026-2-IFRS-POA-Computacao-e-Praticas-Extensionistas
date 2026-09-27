package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;
import ifrs.edu.avaliacao_mnr.dto.ProjectImportResponseDTO;
import ifrs.edu.avaliacao_mnr.dto.ProjectResponseDTO;
import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.event.entity.EventStatus;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;
import ifrs.edu.avaliacao_mnr.project.entity.Project;
import ifrs.edu.avaliacao_mnr.project.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectImportService {

    private static final Logger log = LoggerFactory.getLogger(ProjectImportService.class);

    private final CsvParserService csvParserService;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;

    public ProjectImportService(CsvParserService csvParserService,
                                EventRepository eventRepository,
                                ProjectRepository projectRepository) {
        this.csvParserService = csvParserService;
        this.eventRepository = eventRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectImportResponseDTO importProjectsFromCsv(MultipartFile file, Long eventId, String eventName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file must not be empty.");
        }

        // 1. Resolve Event
        Event event = resolveEvent(file, eventId, eventName);

        // 2. Parse CSV
        List<ProjectImportDTO> parsedDtos = csvParserService.parseCsv(file);

        // 3. Persist / Update Projects (Idempotent)
        int totalProcessed = 0;
        int totalCreated = 0;
        int totalUpdated = 0;
        int totalMarkedForReview = 0;
        List<ProjectResponseDTO> resultProjects = new ArrayList<>();

        for (ProjectImportDTO dto : parsedDtos) {
            totalProcessed++;

            if (dto.markedForReview()) {
                totalMarkedForReview++;
            }

            Optional<Project> existingOpt = projectRepository.findByEventIdAndNameIgnoreCase(event.getId(), dto.projectName());
            Project project;
            if (existingOpt.isPresent()) {
                project = existingOpt.get();
                totalUpdated++;
            } else {
                project = new Project();
                project.setEvent(event);
                project.setName(dto.projectName());
                project.setCreatedAt(LocalDateTime.now());
                totalCreated++;
            }

            project.setPdfUrl(dto.pdfUrl());
            project.setLevel(dto.level());
            project.setVideoUrl(dto.videoUrl());
            project.setParticipantName(dto.participantName());
            project.setParticipantCpf(dto.participantCpf());
            project.setParticipantEmail(dto.participantEmail());
            project.setInstitutionName(dto.institutionName());
            project.setMarkedForReview(dto.markedForReview());
            project.setValidated(dto.validated());

            Project saved = projectRepository.save(project);
            resultProjects.add(ProjectResponseDTO.fromEntity(saved));
        }

        log.info("CSV Import finished for event '{}' (ID: {}). Processed: {}, Created: {}, Updated: {}, Marked for review: {}",
                event.getName(), event.getId(), totalProcessed, totalCreated, totalUpdated, totalMarkedForReview);

        return new ProjectImportResponseDTO(
                event.getId(),
                event.getName(),
                totalProcessed,
                totalCreated,
                totalUpdated,
                totalMarkedForReview,
                resultProjects
        );
    }

    private Event resolveEvent(MultipartFile file, Long eventId, String eventName) {
        if (eventId != null) {
            return eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        }

        String targetName = eventName;
        if (targetName == null || targetName.isBlank()) {
            targetName = csvParserService.extractEventName(file);
        }

        if (targetName == null || targetName.isBlank()) {
            targetName = "Evento Padrão";
        }

        final String finalName = targetName.trim();
        return eventRepository.findByNameIgnoreCase(finalName)
                .orElseGet(() -> {
                    Event newEvent = new Event();
                    newEvent.setName(finalName);
                    newEvent.setDescription("Criado automaticamente via importação CSV");
                    newEvent.setDate(LocalDate.now());
                    newEvent.setStatus(EventStatus.OPEN);
                    newEvent.setCreatedAt(LocalDateTime.now());
                    return eventRepository.save(newEvent);
                });
    }
}
