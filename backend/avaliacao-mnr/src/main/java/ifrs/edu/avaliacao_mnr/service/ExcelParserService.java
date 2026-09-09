package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ExcelParserService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParserService.class);

    @Autowired
    private PdfPageValidationService pdfPageValidationService;

    public List<ProjectImportDTO> parseExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }

        List<ProjectImportDTO> projects = new ArrayList<>();
        int totalLines = 0;
        int ignored = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("The spreadsheet is empty (no header).");
            }

            Map<String, Integer> headerToColumn = new LinkedHashMap<>();
            List<String> originalHeaders = new ArrayList<>();
            for (Cell cell : headerRow) {
                String headerName = formatter.formatCellValue(cell).trim();
                if (!headerName.isEmpty()) {
                    headerToColumn.putIfAbsent(headerName, cell.getColumnIndex());
                    originalHeaders.add(headerName);
                }
            }

            Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(originalHeaders);
            warnMissingColumns(fieldToHeader);

            for (int rowNumber = headerRow.getRowNum() + 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                Row currentRow = sheet.getRow(rowNumber);
                if (currentRow == null) {
                    continue; 
                }
                totalLines++;

                // 1. Excel cell extractor function
                Function<String, String> valueExtractor = header -> {
                    Integer columnIndex = headerToColumn.get(header);
                    if (columnIndex == null) return null;
                    Cell cell = currentRow.getCell(columnIndex);
                    return cell == null ? null : formatter.formatCellValue(cell);
                };

                // 2. Extract data for validation
                String colLevel = fieldToHeader.get("level");
                String level = colLevel == null ? null : valueExtractor.apply(colLevel);

                String colPdf = fieldToHeader.get("pdfUrl");
                String pdfUrl = colPdf == null ? null : valueExtractor.apply(colPdf);

                // 3. Validation rule with protection
                boolean isValid = false;
                try {
                    if (level != null && pdfUrl != null && !pdfUrl.isBlank()) {
                        int pages = pdfPageValidationService.getPdfPages(pdfUrl);
                        isValid = pdfPageValidationService.validatePdfPages(level, pages);
                    }
                } catch (Exception e) {
                    log.warn("Validation failed for project on row {}: {}", rowNumber + 1, e.getMessage());
                }

                // 4. Build the DTO
                ProjectImportDTO dto = ProjectRowMapper.buildDto(
                        fieldToHeader,
                        valueExtractor,
                        !isValid, // markedForReview
                        isValid   // validated
                );

                if (dto == null) {
                    ignored++;
                    log.debug("Row {} ignored: project title is missing.", rowNumber + 1);
                    continue;
                }
                projects.add(dto);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the Excel file: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to process the Excel file: " + e.getMessage(), e);
        }

        log.info("Excel import completed: {} line(s) read, {} project(s) imported, {} ignored.", totalLines, projects.size(), ignored);
        return projects;
    }

    private void warnMissingColumns(Map<String, String> fieldToHeader) {
        List<String> missing = ProjectRowMapper.missingFields(fieldToHeader);
        if (!missing.isEmpty()) {
            log.warn("Could not locate the spreadsheet column for the fields {}. They will remain null in all imported projects.", missing);
        }
    }
}