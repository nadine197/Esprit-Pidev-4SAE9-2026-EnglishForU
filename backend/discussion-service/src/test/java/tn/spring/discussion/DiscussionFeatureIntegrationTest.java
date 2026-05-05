package tn.spring.discussion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiscussionFeatureIntegrationTest {

    private static final String JWT_SECRET = "7c1015921c5d90739574de77e2c5bb7661a555f493e0c2b0ca47f550b938368bd7e839ba6ff0dc617716913dfaf61c7bd87b57f3920241f008cd54fb6c7918fa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticatedUserCanCreatePostAndReadMineFeed() throws Exception {
        String token = bearer("student1@englishforu.local", "STUDENT");

        Map<String, Object> payload = Map.of(
                "courseId", "ENG-A2",
                "type", "TEXT",
                "content", "Who wants to practice phrasal verbs?",
                "authorLevel", "A2"
        );

        mockMvc.perform(post("/api/discussions/posts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorEmail").value("student1@englishforu.local"))
                .andExpect(jsonPath("$.authorRole").value("STUDENT"));

        mockMvc.perform(get("/api/discussions/feed")
                        .header("Authorization", token)
                        .param("scope", "mine")
                        .param("viewerLevel", "A2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void userCanCommentAndReactOnVisiblePost() throws Exception {
        String creatorToken = bearer("student2@englishforu.local", "STUDENT");
        String participantToken = bearer("student3@englishforu.local", "STUDENT");

        Map<String, Object> createPayload = Map.of(
                "courseId", "ENG-B1",
                "type", "TEXT",
                "content", "Let's discuss conditionals.",
                "authorLevel", "B1",
                "targetRole", "STUDENT",
                "targetLevel", "B1"
        );

        String createResponse = mockMvc.perform(post("/api/discussions/posts")
                        .header("Authorization", creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        long postId = createJson.get("id").asLong();

        Map<String, Object> commentPayload = Map.of("message", "Great idea, I can join tonight.");
        mockMvc.perform(post("/api/discussions/posts/{postId}/comments", postId)
                        .header("Authorization", participantToken)
                        .param("viewerLevel", "B1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Great idea, I can join tonight."));

        Map<String, Object> reactionPayload = Map.of("type", "LIKE");
        mockMvc.perform(post("/api/discussions/posts/{postId}/reactions", postId)
                        .header("Authorization", participantToken)
                        .param("viewerLevel", "B1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactionPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reactionCount").value(1));

        mockMvc.perform(get("/api/discussions/posts/{postId}", postId)
                        .header("Authorization", participantToken)
                        .param("viewerLevel", "B1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentCount").value(1))
                .andExpect(jsonPath("$.reactionCount").value(1))
                .andExpect(jsonPath("$.myReaction").value("LIKE"));
    }

    @Test
    void roleAudienceFilterBlocksUnauthorizedRole() throws Exception {
        String tutorToken = bearer("tutor1@englishforu.local", "TUTOR");
        String studentToken = bearer("student4@englishforu.local", "STUDENT");

        Map<String, Object> createPayload = Map.of(
                "courseId", "ENG-B2",
                "type", "TEXT",
                "content", "Tutor-only discussion post",
                "targetRole", "TUTOR"
        );

        mockMvc.perform(post("/api/discussions/posts")
                        .header("Authorization", tutorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/discussions/feed")
                        .header("Authorization", studentToken)
                        .param("scope", "all")
                        .param("viewerLevel", "B2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void authorCanUploadImageForOwnPost() throws Exception {
        String authorToken = bearer("student5@englishforu.local", "STUDENT");

        Map<String, Object> createPayload = Map.of(
                "courseId", "ENG-A2",
                "type", "IMAGE",
                "content", "Practice board visual aid",
                "authorLevel", "A2"
        );

        String createResponse = mockMvc.perform(post("/api/discussions/posts")
                        .header("Authorization", authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long postId = objectMapper.readTree(createResponse).get("id").asLong();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "chart.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/discussions/posts/{postId}/image", postId)
                        .file(file)
                        .header("Authorization", authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("IMAGE"))
                .andExpect(jsonPath("$.imagePath").isNotEmpty());
    }

    private String bearer(String email, String role) {
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600_000))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();

        return "Bearer " + token;
    }

    private Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
