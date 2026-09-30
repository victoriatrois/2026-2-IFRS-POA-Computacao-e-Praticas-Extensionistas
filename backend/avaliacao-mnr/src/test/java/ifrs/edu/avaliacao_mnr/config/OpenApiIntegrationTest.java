package ifrs.edu.avaliacao_mnr.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentIsPublicAndIncludesImplementedEndpointsAndJwtScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("MNR Evaluation API"))
                .andExpect(jsonPath("$.paths['/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/auth/refresh'].post").exists())
                .andExpect(jsonPath("$.paths['/auth/logout'].post").exists())
                .andExpect(jsonPath("$.paths['/auth/me'].get").exists())
                .andExpect(jsonPath("$.paths['/users'].get").exists())
                .andExpect(jsonPath("$.paths['/users'].post").exists())
                .andExpect(jsonPath("$.paths['/api/projects'].get").exists())
                .andExpect(jsonPath("$.paths['/api/projects/import'].post").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void swaggerUiEntryPointIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
