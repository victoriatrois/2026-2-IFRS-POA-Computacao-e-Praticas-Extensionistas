package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/*
What this class does:
Utility that centralizes the mapping of spreadsheet columns.
Responsible for normalizing header names, tolerating writing variations (aliases/substrings), and ensuring that both CSV and Excel reading follow the exact same extraction rules.
*/
final class ProjectRowMapper {
    /*
     COLUMN MAPPING DICTIONARY (From -> To)

     The constants below represent the internal fields of our system (DTO). The static block initializes the rules on how to find these columns in the spreadsheet:

     1. EXACT_ALIASES (Exact Match): List of possible exact names for the column.
     - The first item in the list is always the real name used in the system's current export.
     - The others are safe variations to prevent breaking if the spreadsheet changes.

     2. CONTAINS_FALLBACK (Partial Match): Used in case the exact match fails.
     - Looks for keywords (e.g., "pdf", "cpf") inside the header.
     - Applied only to exclusive fields. It is not used for fields like "name", because the word appears in many columns (participant, project, institution), which would cause false positives.

     NOTE: Currently, the CPF column ("person.cpf") comes empty in the export. Therefore, it is best to evaluate using an email or UUID as the primary identifier in the future.
     */

    static final String PROJECT_NAME = "projectName";
    static final String PDF_URL = "pdfUrl";
    static final String LEVEL = "level";
    static final String VIDEO_URL = "videoUrl";
    static final String PARTICIPANT_NAME = "participantName";
    static final String PARTICIPANT_CPF = "participantCpf";
    static final String PARTICIPANT_EMAIL = "participantEmail";
    static final String INSTITUTION_NAME = "institutionName";

    private static final Map<String, List<String>> EXACT_ALIASES = new LinkedHashMap<>();
    private static final Map<String, List<String>> CONTAINS_FALLBACK = new LinkedHashMap<>();

    static {
        EXACT_ALIASES.put(PROJECT_NAME, List.of(
                "name", "titulo", "nome_do_projeto", "project_name", "nome_projeto"));

        EXACT_ALIASES.put(PDF_URL, List.of(
                "resumo_artigo_pdf_obrigatorio", "pdf_url", "resumo_pdf", "artigo_pdf"));

        EXACT_ALIASES.put(LEVEL, List.of(
                "qual_e_o_nivel_do_trabalho_inscrito_obrigatorio", "level", "nivel"));

        EXACT_ALIASES.put(VIDEO_URL, List.of(
                "endereco_youtube_link_para_o_video_de_apresentacao_obrigatorio",
                "video_url", "youtube", "video"));

        EXACT_ALIASES.put(PARTICIPANT_NAME, List.of(
                "person_name", "participant_name", "nome_do_participante", "responsavel", "team", "equipe"));

        EXACT_ALIASES.put(PARTICIPANT_CPF, List.of(
                "person_cpf", "cpf", "participant_cpf", "registration_number", "id"));

        EXACT_ALIASES.put(PARTICIPANT_EMAIL, List.of(
                "person_email", "email", "participant_email", "e_mail"));

        EXACT_ALIASES.put(INSTITUTION_NAME, List.of(
                "institution_name", "institution", "instituicao", "escola"));

        CONTAINS_FALLBACK.put(PDF_URL, List.of("pdf"));
        CONTAINS_FALLBACK.put(LEVEL, List.of("nivel"));
        CONTAINS_FALLBACK.put(VIDEO_URL, List.of("video", "youtube"));
        CONTAINS_FALLBACK.put(PARTICIPANT_CPF, List.of("cpf"));
        CONTAINS_FALLBACK.put(PARTICIPANT_EMAIL, List.of("email"));
        CONTAINS_FALLBACK.put(INSTITUTION_NAME, List.of("instit"));
    }

    private ProjectRowMapper() {
    }

    /*
      # Normalizes a header for comparison
      - Removes accents, converts to lowercase, and changes non-alphanumeric characters into underscores
        e.g.: "Qual é o Nível?" becomes "qual_e_o_nivel"
     */
    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String lowerCase = withoutAccents.toLowerCase(Locale.ROOT);
        String withUnderscore = lowerCase.replaceAll("[^a-z0-9]+", "_");
        return withUnderscore.replaceAll("^_+", "").replaceAll("_+$", "");
    }

    /*
      Maps the file's original headers to the system's internal fields (DTO).
      - To ensure performance when reading spreadsheets with thousands of records, 
      the method is executed only once per file (when reading the 1st row) and not on every row iteration.

      Resolution flow:
        - Normalizes the received file's headers (removes accents, spaces, etc.)
        - For each required field in the system, tries to find the ideal column using an exact match.
        - If the exact match fails, tries to find it by keyword (partial match/substring).

      Notes:
      @param originalHeaders -> List with the header names exactly as they appear in the file.
      @return -> A map where the Key is the system field (e.g.: "projectName") and the Value is
      the real name of the column read from the file (e.g.: "Nome do Projeto").
             NOTE: The value will be NULL if the corresponding column is not found in the spreadsheet.
     */
    static Map<String, String> resolveHeaderMap(Collection<String> originalHeaders) {
        Map<String, String> normalizedToOriginal = new LinkedHashMap<>();
        for (String original : originalHeaders) {
            normalizedToOriginal.putIfAbsent(normalize(original), original);
        }

        Map<String, String> fieldToHeader = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : EXACT_ALIASES.entrySet()) {
            String field = entry.getKey();
            String found = exactMatch(entry.getValue(), normalizedToOriginal);
            if (found == null) {
                found = substringMatch(CONTAINS_FALLBACK.get(field), normalizedToOriginal);
            }
            fieldToHeader.put(field, found);
        }
        return fieldToHeader;
    }

    /*
      # Converts data from a single spreadsheet row into a ProjectImportDTO object.
      - The big advantage of this method is that it works for both CSV files and Excel spreadsheets. 
      It does not need to know the file format: it just receives a ready rule (the valueByHeader method)
      that already knows how to get the correct info from the column in the current row being read.

      Validation Rule: Rows that do not have the project title filled are considered "dirty" or empty data.
      In these cases, the conversion is aborted to avoid generating garbage in the database.

      Notes:
       @param fieldToHeader -> The map resolved by {@link #resolveHeaderMap}, linking the system fields to the real spreadsheet column names.
       @param valueByHeader -> A lambda function that receives a header name and returns the value contained in the cell of the current row.
      @return -> The populated ProjectImportDTO object with formatted data or NULL.
     */
    static ProjectImportDTO buildDto(Map<String, String> fieldToHeader, Function<String, String> valueByHeader) {
        String projectName = cleanValue(resolve(fieldToHeader, valueByHeader, PROJECT_NAME));
        if (projectName == null) {
            return null; // missing title -> "dirty" line, should be ignored
        }

        return new ProjectImportDTO(
                projectName,
                cleanValue(resolve(fieldToHeader, valueByHeader, PDF_URL)),
                cleanValue(resolve(fieldToHeader, valueByHeader, LEVEL)),
                cleanValue(resolve(fieldToHeader, valueByHeader, VIDEO_URL)),
                cleanValue(resolve(fieldToHeader, valueByHeader, PARTICIPANT_NAME)),
                cleanCpf(resolve(fieldToHeader, valueByHeader, PARTICIPANT_CPF)),
                cleanValue(resolve(fieldToHeader, valueByHeader, PARTICIPANT_EMAIL)),
                cleanValue(resolve(fieldToHeader, valueByHeader, INSTITUTION_NAME))
        );
    }

    // Lists the DTO fields for which no column was found in the file
    static List<String> missingFields(Map<String, String> fieldToHeader) {
        List<String> missing = new ArrayList<>();
        fieldToHeader.forEach((field, header) -> {
            if (header == null) {
                missing.add(field);
            }
        });
        return missing;
    }

    private static String exactMatch(List<String> aliases, Map<String, String> normalizedToOriginal) {
        for (String alias : aliases) {
            String original = normalizedToOriginal.get(normalize(alias));
            if (original != null) {
                return original;
            }
        }
        return null;
    }

    private static String substringMatch(List<String> tokens, Map<String, String> normalizedToOriginal) {
        if (tokens == null) {
            return null;
        }
        for (String token : tokens) {
            for (Map.Entry<String, String> entry : normalizedToOriginal.entrySet()) {
                if (entry.getKey().contains(token)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static String resolve(Map<String, String> fieldToHeader, Function<String, String> valueByHeader, String field) {
        String header = fieldToHeader.get(field);
        return header == null ? null : valueByHeader.apply(header);
    }

    private static String cleanValue(String value) {
        if (value == null) {
            return null;
        }
        String withoutSpaces = value.trim();
        return withoutSpaces.isEmpty() ? null : withoutSpaces;
    }

    // Removes everything that is not a digit (dots, dashes, spaces) from a CPF.
    private static String cleanCpf(String value) {
        String cleaned = cleanValue(value);
        if (cleaned == null) {
            return null;
        }
        String onlyDigits = cleaned.replaceAll("\\D", "");
        return onlyDigits.isEmpty() ? null : onlyDigits;
    }
}