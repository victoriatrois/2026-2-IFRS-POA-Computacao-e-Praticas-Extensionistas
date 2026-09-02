package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.service.VideoAnalyzerService;
import org.springframework.web.bind.annotation.*;

// TODO remove, endpoint just to test the url validation flow
@RestController
@RequestMapping("/videos")
public class VideoAnalyzerController {

    private final VideoAnalyzerService videoAnalyzerService;

    public VideoAnalyzerController(VideoAnalyzerService videoAnalyzerService) {
        this.videoAnalyzerService = videoAnalyzerService;
    }

    @GetMapping("/validate")
    public String validate(@RequestParam String url) {
        return "The video with url " + url + ".\n Result: " + videoAnalyzerService.hasValidDuration(url);
    }
}
