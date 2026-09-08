package ifrs.edu.avaliacao_mnr.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "youtubeClient", url = "https://youtu.be")
public interface YoutubeClient {
    @GetMapping("/{videoId}")
    String getVideoPage(@PathVariable("videoId") String videoId);
}
