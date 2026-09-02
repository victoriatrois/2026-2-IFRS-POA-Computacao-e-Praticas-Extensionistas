package ifrs.edu.avaliacao_mnr.service;

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
    /*
      # What this class does?
      Processes an uploaded CSV file and converts its rows into DTOs.
      Automatically detects the delimiter, prevents accidental upload of binary files (like .xlsx), 
      and converts valid rows into ProjectImportDTO objects.
    */
    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);

    // ZIP file signature — .xlsx, .docx, and .pptx are all ZIPs under the hood.
    private static final byte[] ZIP_SIGNATURE = {0x50, 0x4B}; // "PK"

    public List<ProjectImportDTO> parseCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }

        String content = readAsText(file);
        char delimiter = detectDelimiter(content);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader() // detects the header on the 1st row
                .setSkipHeaderRecord(true) // does not treat the header as data
                .setIgnoreHeaderCase(true) // "Name", "NAME", and "name" are equivalent
                .setIgnoreEmptyLines(true) // skips entirely blank lines
                .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL) // a repeated header won't crash the process (.setAllowDuplicateHeaderNames(true)i is deprecated)
                .setTrim(true) // removes leading/trailing spaces from each value
                .build();

        List<ProjectImportDTO> projects = new ArrayList<>();
        int totalLines = 0;
        int ignored = 0;

        try (CSVParser csvParser = new CSVParser(new StringReader(content), format)) {

            Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(csvParser.getHeaderNames());
            warnMissingColumns(fieldToHeader);

            for (CSVRecord record : csvParser) {
                totalLines++;

                ProjectImportDTO dto = ProjectRowMapper.buildDto(
                        fieldToHeader,
                        header -> record.isMapped(header) ? record.get(header) : null
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

    /*
      - Reads the entire file as UTF-8 text, rejects binary files -> rejectIfBinary(bytes)
      - Removes the BOM (byte order mark): exports made by Excel on Windows often insert this at the beginning of the file. If not removed, it corrupts the name of the first header column.
     */
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

    /*
     # Defensive barrier
      - Checks the ZIP signature of every .xlsx/.docx/.pptx file (first 2 bytes), instead of relying solely on the file extension.
     */
    private void rejectIfBinary(byte[] bytes) {
        if (bytes.length >= ZIP_SIGNATURE.length
                && bytes[0] == ZIP_SIGNATURE[0]
                && bytes[1] == ZIP_SIGNATURE[1]) {
            throw new IllegalArgumentException("" +
                    "The uploaded file appears to be an .xlsx, not a text CSV. " +
                    "Please use the ExcelParserService for .xlsx files.");
        }
    }

    /*
      # Choice between COMMA or SEMICOLON
      - Spreadsheets exported with Excel configured in pt-BR usually save CSV separated by ";" 
      ("," is the decimal separator). This analyzes only the first line (header) and chooses the most frequent separator between "," and ";".
     */
    private char detectDelimiter(String content) {
        int endOfFirstLine = content.indexOf('\n');
        String firstLine = endOfFirstLine >= 0 ? content.substring(0, endOfFirstLine) : content;

        long commas = firstLine.chars().filter(c -> c == ',').count();
        long semicolons = firstLine.chars().filter(c -> c == ';').count();

        return semicolons > commas ? ';' : ',';
    }

    /*
      # Configured alert
      - Warns if the spreadsheet is incomplete.
      - It uses the ProjectRowMapper to check which fields could not be found and stores them in the "missing" list. If the list is not empty, a warning is sent.
    */
    private void warnMissingColumns(Map<String, String> fieldToHeader) {
        List<String> missing = ProjectRowMapper.missingFields(fieldToHeader);
        if (!missing.isEmpty()) {
            log.warn("Could not locate the CSV column for the fields {}. " +
                    "They will remain null in all imported projects.", missing);
        }
    }
}
