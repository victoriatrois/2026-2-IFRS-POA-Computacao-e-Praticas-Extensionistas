package ifrs.edu.avaliacao_mnr.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ifrs.edu.avaliacao_mnr.event.entity.Event;
import ifrs.edu.avaliacao_mnr.event.entity.EventStatus;
import ifrs.edu.avaliacao_mnr.event.repository.EventRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class DevelopmentSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void developmentProfileAllowsProjectListingWithoutAccessToken() throws Exception {
        Event e = new Event();
        e.setName("dev_event");
        e.setDate(LocalDate.now());
        e.setStatus(EventStatus.OPEN);
        e.setCreatedAt(LocalDateTime.now());
        e = eventRepository.save(e);

        mockMvc.perform(get("/api/events/" + e.getId() + "/projects"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test"))
            .andExpect(status().isOk());
    }
}
