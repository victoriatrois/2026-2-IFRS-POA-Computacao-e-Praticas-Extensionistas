package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;
import ifrs.edu.avaliacao_mnr.project.repository.ProjectRepository;
import ifrs.edu.avaliacao_mnr.service.PdfPageValidationService;
import ifrs.edu.avaliacao_mnr.service.VideoAnalyzerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private ProjectRepository projectRepository;

        @MockitoBean
        private PdfPageValidationService pdfPageValidationService;

        @MockitoBean
        private VideoAnalyzerService videoAnalyzerService;

        @BeforeEach
        void setUp() {
                projectRepository.deleteAll();
                eventRepository.deleteAll();
        }

        // #44: removed old /api/projects route; auth test now targets the new nested
        // route.
        @Test
        void shouldRejectProjectReadsWithoutAuthentication() throws Exception {
                mockMvc.perform(get("/api/events/{eventId}/projects", 1L))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldImportProjectsFromInscricoesTesteCsvSuccessfully() throws Exception {
                when(pdfPageValidationService.getPdfPages(anyString())).thenReturn(2);
                when(pdfPageValidationService.validatePdfPages(anyString(), anyInt())).thenReturn(true);
                when(videoAnalyzerService.hasValidDuration(anyString())).thenReturn(true);

                InputStream csvStream = new ClassPathResource("inscricoes_teste.csv").getInputStream();
                byte[] csvBytes = csvStream.readAllBytes();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "inscricoes_teste.csv",
                                "text/csv",
                                csvBytes);

                mockMvc.perform(multipart("/api/projects/import")
                                .file(file)
                                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                                .authorities(() -> "PROJECT_IMPORT", () -> "PROJECT_READ", () -> "EVENT_READ")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.eventName", is("Etapa Nacional")))
                                .andExpect(jsonPath("$.totalProcessed", greaterThan(0)))
                                .andExpect(jsonPath("$.totalCreated", greaterThan(0)))
                                .andExpect(jsonPath("$.projects", hasSize(greaterThan(0))))
                                .andExpect(jsonPath("$.projects[0].name", notNullValue()));

                assertEquals(1, eventRepository.count());

                Long eventId = eventRepository.findAll().get(0).getId();
                long projectCount = projectRepository.count();
                assertEquals(projectCount, projectRepository.findByEventId(eventId).size());

                // Test idempotency: re-running the same import should not duplicate projects
                mockMvc.perform(multipart("/api/projects/import")
                                .file(file)
                                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                                .authorities(() -> "PROJECT_IMPORT", () -> "PROJECT_READ", () -> "EVENT_READ")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalCreated", is(0)))
                                .andExpect(jsonPath("$.totalUpdated", is((int) projectCount)));

                assertEquals(projectCount, projectRepository.count());

                // Listing is now nested by event and paginated: response is a Page object with
                // items in "content"
                mockMvc.perform(get("/api/events/{eventId}/projects?size=100", eventId)
                                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                                .authorities(() -> "PROJECT_IMPORT", () -> "PROJECT_READ", () -> "EVENT_READ")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize((int) projectCount)))
                                .andExpect(jsonPath("$.content[0].name", notNullValue()));
        }

        // Non-existent eventId returns 404, not an empty list
        @Test
        void shouldReturn404WhenEventDoesNotExistForProjectListing() throws Exception {
                mockMvc.perform(get("/api/events/{eventId}/projects", 999_999L)
                                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                                .authorities(() -> "PROJECT_READ", () -> "EVENT_READ")))
                                .andExpect(status().isNotFound());
        }

        // New GET /api/projects/{id} endpoint with non-existent id
        @Test
        void shouldReturn404WhenProjectDoesNotExist() throws Exception {
                mockMvc.perform(get("/api/projects/{id}", 999_999L)
                                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                                .authorities(() -> "PROJECT_READ", () -> "EVENT_READ")))
                                .andExpect(status().isNotFound());

        }
}
