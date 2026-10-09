package ifrs.edu.avaliacao_mnr.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Same mocking approach as CsvParserServiceTest: ProjectValidationService is
 * mocked to avoid real network calls during the test.
 *
 * The .xlsx file is built in memory with Apache POI, following the same pattern
 * used in PdfPageValidationServiceTest with PDDocument/PDPage.
 */
@ExtendWith(MockitoExtension.class)
class ExcelParserServiceTest {

    @Mock
    private ProjectValidationService projectValidationService;

    private ExcelParserService excelParserService;

    @BeforeEach
    void setUp() {
        excelParserService = new ExcelParserService(projectValidationService);

        lenient().when(projectValidationService.validateProject(any()))
                .thenAnswer(invocation -> {
                    ProjectImportDTO raw = invocation.getArgument(0);
                    return raw.withValidation(false, true);
                });
    }

    @Test
    void parsesValidRow() throws IOException {
        byte[] file = buildXlsx(
                new String[] { "Name", "Nivel", "Responsavel" },
                new String[] { "Robo Seguidor", "2", "Fulano de Tal" });

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "projetos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                file);

        List<ProjectImportDTO> result = excelParserService.parseExcel(multipartFile);

        assertEquals(1, result.size());
        assertEquals("Robo Seguidor", result.get(0).projectName());
        assertEquals("Fulano de Tal", result.get(0).participantName());
        assertTrue(result.get(0).validated());
    }

    @Test
    void ignoresRowWithoutProjectName() throws IOException {
        byte[] file = buildXlsx(
                new String[] { "Name", "Nivel" },
                new String[] { "", "3" });

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "projetos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                file);

        List<ProjectImportDTO> result = excelParserService.parseExcel(multipartFile);

        assertTrue(result.isEmpty());
    }

    @Test
    void emptyFileThrows() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "vazio.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> excelParserService.parseExcel(multipartFile));
    }

    private byte[] buildXlsx(String[] headers, String[] values) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Projetos");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row dataRow = sheet.createRow(1);
            for (int i = 0; i < values.length; i++) {
                dataRow.createCell(i).setCellValue(values[i]);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}