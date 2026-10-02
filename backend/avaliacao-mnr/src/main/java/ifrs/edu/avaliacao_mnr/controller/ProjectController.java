package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportResponseDTO;
import ifrs.edu.avaliacao_mnr.dto.ProjectResponseDTO;
import ifrs.edu.avaliacao_mnr.service.ProjectImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project listing and event project import")
public class ProjectController {

    private final ProjectImportService projectImportService;

    public ProjectController(ProjectImportService projectImportService) {
        this.projectImportService = projectImportService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROJECT_IMPORT')")
        @Operation(summary = "Import projects from CSV", description = "Uploads and validates a project CSV file. Requires the ADMIN project-import permission.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File processed; import summary returned"),
            @ApiResponse(responseCode = "400", description = "File or import data is invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "ADMIN project-import permission required")
        })
    public ResponseEntity<ProjectImportResponseDTO> importProjects(
            @Parameter(description = "CSV file containing registrations", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Existing event identifier; optional when eventName is supplied", required = false)
            @RequestParam(value = "eventId", required = false) Long eventId,
            @Parameter(description = "Event name to use for the import when eventId is omitted", required = false)
            @RequestParam(value = "eventName", required = false) String eventName
    ) {
        ProjectImportResponseDTO response = projectImportService.importProjects(file, eventId, eventName);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_READ')")
        @Operation(summary = "List projects", description = "Lists projects, optionally filtered by event. Available to ADMIN and EVALUATOR users.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Project-read permission required")
        })
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects(
            @Parameter(description = "Optional event identifier filter", required = false)
            @RequestParam(value = "eventId", required = false) Long eventId
    ) {
        List<ProjectResponseDTO> projects = projectImportService.getAllProjects(eventId);
        return ResponseEntity.ok(projects);
    }
}
