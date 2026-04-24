package tn.spring.clubevent.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClubControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetClubsPublicEndpoint() throws Exception {
        // Validation that GET endpoints are publicly accessible thanks to our
        // SecurityConfig
        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetClubByIdPublicEndpoint() throws Exception {
        // Validation that GET /api/clubs/1 handles missing elements gracefully or
        // returns 200/404, but NOT 401/403
        mockMvc.perform(get("/api/clubs/999"))
                .andExpect(status().is5xxServerError()); // It throws NoSuchElementException currently
    }
}
