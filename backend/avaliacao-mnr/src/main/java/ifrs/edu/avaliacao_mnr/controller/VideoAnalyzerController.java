package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.service.VideoAnalyzerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

// TODO remove, endpoint just to test the url validation flow
@RestController
@RequestMapping("/videos")
@Tag(name = "Video Analysis", description = "Video URL and duration validation")
public class VideoAnalyzerController {

    private final VideoAnalyzerService videoAnalyzerService;

    public VideoAnalyzerController(VideoAnalyzerService videoAnalyzerService) {
        this.videoAnalyzerService = videoAnalyzerService;
    }

    @GetMapping("/validate")
        @Operation(summary = "Validate a video URL", description = "Checks whether the video duration is within the configured limits.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result returned"),
            @ApiResponse(responseCode = "400", description = "URL parameter is missing or invalid"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired")
        })
        public String validate(@Parameter(description = "Video URL to validate", required = true)
                   @RequestParam String url) {
        return "The video with url " + url + ".\n Result: " + videoAnalyzerService.hasValidDuration(url);
    }
}
