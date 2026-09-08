package ifrs.edu.avaliacao_mnr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "video.analyzer")
public record VideoAnalyzerProperties(
        int minDurationSeconds,
        int maxDurationSeconds
) {}
