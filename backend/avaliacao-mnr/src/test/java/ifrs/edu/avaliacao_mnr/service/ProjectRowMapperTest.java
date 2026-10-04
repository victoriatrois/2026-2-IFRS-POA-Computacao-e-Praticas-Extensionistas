package ifrs.edu.avaliacao_mnr.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProjectRowMapper is package-private (no "public" on the class), so this
 * test MUST live in ifrs.edu.avaliacao_mnr.service — same package as the
 * class, even though it's under src/test.
 */
class ProjectRowMapperTest {

    @Test
    void resolvesExactAlias() {
        // normalize("Nivel") -> "nivel", which is in LEVEL's EXACT_ALIASES.
        List<String> headers = List.of("Name", "Nivel");
        Map<String, String> row = Map.of(
                "Name", "Robô Seguidor de Linha",
                "Nivel", "2");

        Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(headers);
        var dto = ProjectRowMapper.buildDto(fieldToHeader, row::get);

        assertNotNull(dto);
        assertEquals("Robô Seguidor de Linha", dto.projectName());
        assertEquals("2", dto.level());
    }

    @Test
    void missingProjectNameReturnsNull() {
        // "nome_da_escola" does not match any PROJECT_NAME alias and has no
        // CONTAINS_FALLBACK. Without a project name, buildDto returns null.
        List<String> headers = List.of("nome_da_escola");
        Map<String, String> row = Map.of("nome_da_escola", "IFRS Campus Porto Alegre");

        Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(headers);
        var dto = ProjectRowMapper.buildDto(fieldToHeader, row::get);

        assertNull(dto);
    }

    @Test
    void identifiesEventHeader() {
        // EVENT_HEADER_ALIASES = event_full_name, event_name, evento, event.
        assertTrue(ProjectRowMapper.isEventHeader("Evento"));
        assertTrue(ProjectRowMapper.isEventHeader("Event Name"));
        assertFalse(ProjectRowMapper.isEventHeader("participante"));
    }

    @Test
    void listsUnmatchedFields() {
        // "participante" does not match any PARTICIPANT_NAME alias, so PROJECT_NAME and
        // PARTICIPANT_NAME have no matching columns.
        List<String> headers = List.of("participante");

        Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(headers);
        List<String> missing = ProjectRowMapper.missingFields(fieldToHeader);

        assertTrue(missing.contains(ProjectRowMapper.PROJECT_NAME));
        assertTrue(missing.contains(ProjectRowMapper.PARTICIPANT_NAME));
    }

    @Test
    void cleansCpf() {
        // Exact alias for PARTICIPANT_CPF includes "cpf".
        List<String> headers = List.of("Name", "Cpf");
        Map<String, String> row = Map.of(
                "Name", "Robô Seguidor de Linha",
                "Cpf", "123.456.789-00");

        Map<String, String> fieldToHeader = ProjectRowMapper.resolveHeaderMap(headers);
        var dto = ProjectRowMapper.buildDto(fieldToHeader, row::get);

        assertNotNull(dto);
        assertEquals("12345678900", dto.participantCpf());
    }
}