package ifrs.edu.avaliacao_mnr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

import ifrs.edu.avaliacao_mnr.config.VideoAnalyzerProperties;

@SpringBootApplication
@EnableConfigurationProperties(VideoAnalyzerProperties.class)
@EnableFeignClients
public class AvaliacaoMnrApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvaliacaoMnrApplication.class, args);
	}

}
