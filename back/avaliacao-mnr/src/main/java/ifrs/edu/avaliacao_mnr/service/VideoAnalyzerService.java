package ifrs.edu.avaliacao_mnr.service;

import ifrs.edu.avaliacao_mnr.client.YoutubeClient;
import ifrs.edu.avaliacao_mnr.config.VideoAnalyzerProperties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class VideoAnalyzerService {

    private final YoutubeClient youtubeClient;
    private final VideoAnalyzerProperties properties;

    public VideoAnalyzerService(
        YoutubeClient youtubeClient,
        VideoAnalyzerProperties properties
    ) {
        this.youtubeClient = youtubeClient;
        this.properties = properties;
    }

    public boolean hasValidDuration(String youtubeUrl) {
        String videoId = extractVideoId(youtubeUrl);

        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL");
        }

        Long durationInSeconds = getVideoDuration(videoId);

        return (
            durationInSeconds >= properties.minDurationSeconds() &&
            durationInSeconds <= properties.maxDurationSeconds()
        );
    }

    private String extractVideoId(String youtubeUrl) {
        Pattern pattern = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        );

        Matcher matcher = pattern.matcher(youtubeUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private Long getVideoDuration(String videoId) {
        String htmlPageFromYouTube = youtubeClient.getVideoPage(videoId);
        Pattern pattern = Pattern.compile("\"lengthSeconds\":\"(\\d+)\"");

        Matcher matcher = pattern.matcher(htmlPageFromYouTube);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not find video duration");
        }

        return Long.parseLong(matcher.group(1));
    }
}
