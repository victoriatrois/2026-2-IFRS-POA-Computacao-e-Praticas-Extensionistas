package ifrs.edu.avaliacao_mnr.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mnrOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MNR Evaluation API")
                        .version("v1")
                        .description("REST API for authentication, user administration, project import and evaluation workflows."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter the access token returned by POST /auth/login. Do not include the 'Bearer ' prefix.")));
    }
}