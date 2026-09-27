package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportResponseDTO;
import ifrs.edu.avaliacao_mnr.service.ProjectImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectImportService projectImportService;

    public ProjectController(ProjectImportService projectImportService) {
        this.projectImportService = projectImportService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectImportResponseDTO> importProjects(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "eventId", required = false) Long eventId,
            @RequestParam(value = "eventName", required = false) String eventName
    ) {
        ProjectImportResponseDTO response = projectImportService.importProjectsFromCsv(file, eventId, eventName);
        return ResponseEntity.ok(response);
    }
}
