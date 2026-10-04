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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// #44 No class-level `@RequestMapping`. Import remains at `/api/projects/import`, while listing uses `/api/events/{eventId}/projects`, so each method defines its full path.

@RestController
@Tag(name = "Projects", description = "Project listing and event project import")
public class ProjectController {

    private final ProjectImportService projectImportService;

    public ProjectController(ProjectImportService projectImportService) {
        this.projectImportService = projectImportService;
    }

    @PostMapping(value = "/api/projects/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROJECT_IMPORT')")
    @Operation(summary = "Import projects from CSV", description = "Uploads and validates a project from a CSV file. Requires the ADMIN role.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File processed; import summary returned"),
            @ApiResponse(responseCode = "400", description = "File or import data is invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required")
    })
    public ResponseEntity<ProjectImportResponseDTO> importProjects(
            @Parameter(description = "CSV file containing registrations", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Existing event identifier; optional when eventName is supplied", required = false) @RequestParam(value = "eventId", required = false) Long eventId,
            @Parameter(description = "Event name to use for the import when eventId is omitted", required = false) @RequestParam(value = "eventName", required = false) String eventName) {
        ProjectImportResponseDTO response = projectImportService.importProjects(file, eventId, eventName);
        return ResponseEntity.ok(response);
    }

    // Replaced `GET /api/projects?eventId=` (removed).

    @GetMapping("/api/events/{eventId}/projects")
    @PreAuthorize("hasAuthority('PROJECT_READ')")
    @Operation(summary = "List projects by event", description = "Lists projects of one event, paginated. Available to ADMIN and EVALUATOR users.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Project-read permission required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Page<ProjectResponseDTO>> listProjectsByEvent(
            @Parameter(description = "Event identifier", required = true) @PathVariable Long eventId,
            Pageable pageable) {
        Page<ProjectResponseDTO> projects = projectImportService.getProjectsByEvent(eventId, pageable);
        return ResponseEntity.ok(projects);
    }

    // new endpoint
    @GetMapping("/api/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT_READ')")
    @Operation(summary = "Get project by id", description = "Returns a single project. Available to ADMIN and EVALUATOR users.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project returned"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
            @ApiResponse(responseCode = "403", description = "Project-read permission required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @Parameter(description = "Project identifier", required = true) @PathVariable Long id) {
        ProjectResponseDTO project = projectImportService.getProjectById(id);
        return ResponseEntity.ok(project);
    }
}