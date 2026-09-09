package ifrs.edu.avaliacao_mnr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CsvParserService {
    
    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);
    private static final byte[] ZIP_SIGNATURE = {0x50, 0x4B}; 

    @Autowired
    private PdfPageValidationService pdfPageValidationService;

    // TODO: Uncomment when Allan creates the VideoAnalyzeService class
    // @Autowired
    // private VideoAnalyzeService videoAnalyzeService; 

    public List<ProjectImportDTO> parseCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }

        String content = readAsText(file);
        char delimiter = detectDelimiter(content);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader() 
                .setSkipHeaderRecord(true) 
                .setIgnoreHeaderCase(true) 
                .setIgnoreEmptyLines(true) 
                .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL) 
                .setTrim(true) 
                .build();

        List<ProjectImportDTO> projects = new ArrayList<>();
        int totalLines = 0;
        int ignored = 0;

        try (CSVParser csvParser = new CSVParser(new StringReader(content), format)) {

            Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(csvParser.getHeaderNames());
            warnMissingColumns(fieldToHeader);

            for (CSVRecord record : csvParser) {
                totalLines++;

                // 1. Extract data for validation
                String colLevel = fieldToHeader.get("level");
                String level = (colLevel != null && record.isMapped(colLevel)) ? record.get(colLevel) : null;

                String colPdf = fieldToHeader.get("pdfUrl");
                String pdfUrl = (colPdf != null && record.isMapped(colPdf)) ? record.get(colPdf) : null;

                // 2. Validation rule with protection against broken links
                boolean isValid = false;
                try {
                    if (level != null && pdfUrl != null && !pdfUrl.isBlank()) {
                        int pages = pdfPageValidationService.getPdfPages(pdfUrl);
                        isValid = pdfPageValidationService.validatePdfPages(level, pages);
                    }
                } catch (Exception e) {
                    log.warn("Validation failed for project on line {}: {}", record.getRecordNumber(), e.getMessage());
                }

                // 3. Build the DTO with the 4 required parameters
                ProjectImportDTO dto = ProjectRowMapper.buildDto(
                        fieldToHeader,
                        header -> record.isMapped(header) ? record.get(header) : null,
                        !isValid, // markedForReview
                        isValid   // validated
                );

                if (dto == null) {
                    ignored++;
                    log.debug("Row {} ignored: project title is missing.", record.getRecordNumber());
                    continue;
                }
                projects.add(dto);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the CSV file: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to process the CSV file: " + e.getMessage(), e);
        }

        log.info("CSV import completed: {} line(s) read, {} project(s) imported, {} ignored.", totalLines, projects.size(), ignored);
        return projects;
    }

    private String readAsText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            rejectIfBinary(bytes);

            String text = new String(bytes, StandardCharsets.UTF_8);
            if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
                text = text.substring(1);
            }
            return text;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file.", e);
        }
    }

    private void rejectIfBinary(byte[] bytes) {
        if (bytes.length >= ZIP_SIGNATURE.length && bytes[0] == ZIP_SIGNATURE[0] && bytes[1] == ZIP_SIGNATURE[1]) {
            throw new IllegalArgumentException("The uploaded file appears to be an .xlsx, not a text CSV. Please use the ExcelParserService for .xlsx files.");
        }
    }

    private char detectDelimiter(String content) {
        int endOfFirstLine = content.indexOf('\n');
        String firstLine = endOfFirstLine >= 0 ? content.substring(0, endOfFirstLine) : content;

        long commas = firstLine.chars().filter(c -> c == ',').count();
        long semicolons = firstLine.chars().filter(c -> c == ';').count();

        return semicolons > commas ? ';' : ',';
    }

    private void warnMissingColumns(Map<String, String> fieldToHeader) {
        List<String> missing = ProjectRowMapper.missingFields(fieldToHeader);
        if (!missing.isEmpty()) {
            log.warn("Could not locate the CSV column for the fields {}. They will remain null in all imported projects.", missing);
        }
    }
}
