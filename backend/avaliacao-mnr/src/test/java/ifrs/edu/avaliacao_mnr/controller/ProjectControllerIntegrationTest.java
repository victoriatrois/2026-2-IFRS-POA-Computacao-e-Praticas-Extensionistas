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
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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
                csvBytes
        );

        mockMvc.perform(multipart("/api/projects/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName", is("Etapa Nacional")))
                .andExpect(jsonPath("$.totalProcessed", greaterThan(0)))
                .andExpect(jsonPath("$.totalCreated", greaterThan(0)))
                .andExpect(jsonPath("$.projects", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.projects[0].name", notNullValue()));

        assertEquals(1, eventRepository.count());
        long projectCount = projectRepository.count();
        assertEquals(projectCount, projectRepository.findByEventId(eventRepository.findAll().get(0).getId()).size());

        // Test idempotency: re-running the same import should not duplicate projects
        mockMvc.perform(multipart("/api/projects/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCreated", is(0)))
                .andExpect(jsonPath("$.totalUpdated", is((int) projectCount)));

        assertEquals(projectCount, projectRepository.count());

        // Test GET /api/projects
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) projectCount)))
                .andExpect(jsonPath("$[0].name", notNullValue()));
    }
}
