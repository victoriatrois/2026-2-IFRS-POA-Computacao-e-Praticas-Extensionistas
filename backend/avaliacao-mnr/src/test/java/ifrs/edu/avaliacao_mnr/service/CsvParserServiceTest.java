package ifrs.edu.avaliacao_mnr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * ProjectValidationService.validateProject() makes real PDF and YouTube
 * requests. It is mocked here to keep the test independent of the network.
 */
@ExtendWith(MockitoExtension.class)
class CsvParserServiceTest {

    @Mock
    private ProjectValidationService projectValidationService;

    private CsvParserService csvParserService;

    @BeforeEach
    void setUp() {
        csvParserService = new CsvParserService(projectValidationService);

        // lenient(): the empty-file test fails before reaching validateProject(),
        // so this stub is not used by every test. Without lenient(), strict stubbing
        // would throw UnnecessaryStubbingException.
        lenient().when(projectValidationService.validateProject(any()))
                .thenAnswer(invocation -> {
                    ProjectImportDTO raw = invocation.getArgument(0);
                    return raw.withValidation(false, true);
                });
    }

    @Test
    void parsesValidRow() {
        String content = "Name,Nivel,Responsavel\nRobo Seguidor,2,Fulano de Tal\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "projetos.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));

        List<ProjectImportDTO> result = csvParserService.parseCsv(file);

        assertEquals(1, result.size());
        assertEquals("Robo Seguidor", result.get(0).projectName());
        assertEquals("Fulano de Tal", result.get(0).participantName());
        assertTrue(result.get(0).validated());
    }

    @Test
    void ignoresRowWithoutProjectName() {
        // "Name" header exists, but the row's cell is empty -> project
        // without a title is ignored (buildDto returns null).
        String content = "Name,Nivel\n,3\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "projetos.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));

        List<ProjectImportDTO> result = csvParserService.parseCsv(file);

        assertTrue(result.isEmpty());
    }

    @Test
    void emptyFileThrows() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "vazio.csv", "text/csv", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> csvParserService.parseCsv(file));
    }
}