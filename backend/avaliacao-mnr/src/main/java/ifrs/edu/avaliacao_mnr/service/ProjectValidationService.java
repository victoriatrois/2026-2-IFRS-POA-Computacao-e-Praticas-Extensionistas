package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.dto.ProjectImportDTO;
import ifrs.edu.avaliacao_mnr.model.ProjectValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectValidationService {

    private static final Logger log = LoggerFactory.getLogger(ProjectValidationService.class);

    private final PdfPageValidationService pdfPageValidationService;
    private final VideoAnalyzerService videoAnalyzerService;

    public ProjectValidationService(PdfPageValidationService pdfPageValidationService,
                                  VideoAnalyzerService videoAnalyzerService) {
        this.pdfPageValidationService = pdfPageValidationService;
        this.videoAnalyzerService = videoAnalyzerService;
    }

    public ProjectValidationResult validate(String level, String pdfUrl, String videoUrl) {
        return validate(level, pdfUrl, videoUrl, null);
    }

    public ProjectValidationResult validate(String level, String pdfUrl, String videoUrl, String projectName) {
        boolean isPdfValid = false;
        try {
            if (level != null && !level.isBlank() && pdfUrl != null && !pdfUrl.isBlank()) {
                int pages = pdfPageValidationService.getPdfPages(pdfUrl);
                isPdfValid = pdfPageValidationService.validatePdfPages(level, pages);
            }
        } catch (Exception e) {
            log.warn("PDF validation failed for project '{}': {}", projectName != null ? projectName : "unnamed", e.getMessage());
        }

        boolean isVideoValid = false;
        try {
            if (videoUrl != null && !videoUrl.isBlank()) {
                isVideoValid = videoAnalyzerService.hasValidDuration(videoUrl);
            }
        } catch (Exception e) {
            log.warn("Video validation failed for project '{}': {}", projectName != null ? projectName : "unnamed", e.getMessage());
        }

        boolean isValid = isPdfValid && isVideoValid;
        return new ProjectValidationResult(isPdfValid, isVideoValid, isValid);
    }

    public ProjectImportDTO validateProject(ProjectImportDTO dto) {
        if (dto == null) {
            return null;
        }

        ProjectValidationResult result = validate(dto.level(), dto.pdfUrl(), dto.videoUrl(), dto.projectName());
        return dto.withValidation(!result.isValid(), result.isValid());
    }

    public boolean isValid(String level, String pdfUrl, String videoUrl) {
        return validate(level, pdfUrl, videoUrl).isValid();
    }

    public boolean isValid(ProjectImportDTO dto) {
        if (dto == null) {
            return false;
        }
        return validate(dto.level(), dto.pdfUrl(), dto.videoUrl(), dto.projectName()).isValid();
    }
}
