package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;
import ifrs.edu.avaliacao_mnr.dto.ProjectImportResponseDTO;
import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.event.entity.EventStatus;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;
import ifrs.edu.avaliacao_mnr.project.entity.Project;
import ifrs.edu.avaliacao_mnr.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectImportServiceTest {

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ProjectRepository projectRepository;

    private ProjectImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ProjectImportService(csvParserService, eventRepository, projectRepository);
    }

    @Test
    void shouldCreateNewEventAndInsertProjectsWhenEventNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "teste.csv", "text/csv", "name;status\nProjeto 1;approved".getBytes()
        );

        when(csvParserService.extractEventName(file)).thenReturn("Etapa Regional");
        when(eventRepository.findByNameIgnoreCase("Etapa Regional")).thenReturn(Optional.empty());

        Event savedEvent = new Event();
        savedEvent.setId(10L);
        savedEvent.setName("Etapa Regional");
        savedEvent.setDate(LocalDate.now());
        savedEvent.setStatus(EventStatus.OPEN);
        savedEvent.setCreatedAt(LocalDateTime.now());
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        ProjectImportDTO dto1 = new ProjectImportDTO(
                "Projeto 1", "http://pdf", "n1", "http://youtube", "Aluno 1",
                "12345678901", "aluno@email.com", "Escola X", false, true
        );
        when(csvParserService.parseCsv(file)).thenReturn(List.of(dto1));
        when(projectRepository.findByEventIdAndNameIgnoreCase(10L, "Projeto 1")).thenReturn(Optional.empty());

        Project savedProject = new Project();
        savedProject.setId(100L);
        savedProject.setName("Projeto 1");
        savedProject.setEvent(savedEvent);
        savedProject.setValidated(true);
        savedProject.setMarkedForReview(false);
        savedProject.setCreatedAt(LocalDateTime.now());
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectImportResponseDTO response = importService.importProjectsFromCsv(file, null, null);

        assertNotNull(response);
        assertEquals(10L, response.eventId());
        assertEquals("Etapa Regional", response.eventName());
        assertEquals(1, response.totalProcessed());
        assertEquals(1, response.totalCreated());
        assertEquals(0, response.totalUpdated());
        assertEquals(0, response.totalMarkedForReview());
        assertEquals(1, response.projects().size());
        assertEquals("Projeto 1", response.projects().get(0).name());
    }

    @Test
    void shouldUpdateExistingProjectWhenAlreadyPresentInEvent() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "teste.csv", "text/csv", "content".getBytes()
        );

        Event existingEvent = new Event();
        existingEvent.setId(20L);
        existingEvent.setName("Etapa Nacional");
        when(eventRepository.findById(20L)).thenReturn(Optional.of(existingEvent));

        ProjectImportDTO dto = new ProjectImportDTO(
                "Robô Seguidor", "http://pdf", "n2", "http://youtube", "Aluno 2",
                "98765432100", "aluno2@email.com", "Escola Y", true, false
        );
        when(csvParserService.parseCsv(file)).thenReturn(List.of(dto));

        Project existingProject = new Project();
        existingProject.setId(200L);
        existingProject.setEvent(existingEvent);
        existingProject.setName("Robô Seguidor");
        existingProject.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(projectRepository.findByEventIdAndNameIgnoreCase(20L, "Robô Seguidor"))
                .thenReturn(Optional.of(existingProject));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectImportResponseDTO response = importService.importProjectsFromCsv(file, 20L, null);

        assertNotNull(response);
        assertEquals(20L, response.eventId());
        assertEquals(1, response.totalProcessed());
        assertEquals(0, response.totalCreated());
        assertEquals(1, response.totalUpdated());
        assertEquals(1, response.totalMarkedForReview());
        assertEquals(1, response.projects().size());
        assertTrue(response.projects().get(0).markedForReview());
        assertFalse(response.projects().get(0).validated());
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> importService.importProjectsFromCsv(emptyFile, 1L, null));
    }
}
