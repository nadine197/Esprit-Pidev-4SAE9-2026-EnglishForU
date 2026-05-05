package tn.spring.course.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.course.Clients.UserClient;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.DTO.UserDTO;
import tn.spring.course.Exceptions.ResourceNotFoundException;
import tn.spring.course.Exceptions.UnauthorizedException;
import tn.spring.course.Mappers.StudyGroupMapper;
import tn.spring.course.Models.Course;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de la communication OpenFeign entre le microservice Course
 * et le microservice User via UserClient.
 *
 * Stratégie : on mocke UserClient (l'interface Feign) avec Mockito.
 * On teste la méthode createStudyGroupWithValidation() qui appelle
 * userClient.getUserById() avant de créer le groupe.
 */
@ExtendWith(MockitoExtension.class)
class StudyGroupFeignTest {

    @Mock
    private UserClient userClient;          // ← le client OpenFeign mocké

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudyGroupMapper mapper;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    // ── Helpers ───────────────────────────────────────────────

    private String buildUserJson(UUID id, String name, String lastName, String role) {
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"lastName\":\"%s\"," +
            "\"email\":\"test@test.com\",\"role\":\"%s\",\"active\":true}",
            id, name, lastName, role
        );
    }

    private StudyGroupRequestDTO buildRequest(UUID tutorId) {
        StudyGroupRequestDTO dto = new StudyGroupRequestDTO();
        dto.setName("Groupe Test");
        dto.setLevel("INTERMEDIATE");
        dto.setLocation("Tunis");
        dto.setMaxCapacity(15);
        dto.setStatus(StudyGroupStatus.PLANNED);
        dto.setCourseId(1);
        dto.setTutorId(tutorId);
        return dto;
    }

    // ═══════════════════════════════════════════════════════════
    // CAS NOMINAUX — appel Feign réussi
    // ═══════════════════════════════════════════════════════════

    /**
     * Test 1 : Le tuteur existe dans le microservice User.
     * UserClient retourne un JSON valide → le groupe est créé.
     */
    @Test
    void shouldCreateGroupWhenTutorExistsViaFeign() {
        UUID tutorId = UUID.randomUUID();
        String userJson = buildUserJson(tutorId, "Ahmed", "Ben Ali", "TUTOR");

        Course course = new Course();
        course.setCourseid(1);

        StudyGroup group = new StudyGroup();
        group.setName("Groupe Test");

        StudyGroupResponseDTO responseDTO = new StudyGroupResponseDTO();
        responseDTO.setName("Groupe Test");

        // Simuler la réponse du microservice User via Feign
        when(userClient.getUserById(tutorId)).thenReturn(userJson);
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(mapper.toEntity(any(), eq(course))).thenReturn(group);
        when(studyGroupRepository.save(group)).thenReturn(group);
        when(mapper.toDTO(group)).thenReturn(responseDTO);

        StudyGroupResponseDTO result =
                studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId));

        assertNotNull(result);
        assertEquals("Groupe Test", result.getName());

        // Vérifier que Feign a bien été appelé avec le bon UUID
        verify(userClient, times(1)).getUserById(tutorId);
        verify(studyGroupRepository, times(1)).save(group);
    }

    /**
     * Test 2 : Feign est appelé avec le bon UUID (vérification de l'argument).
     */
    @Test
    void shouldCallFeignWithCorrectTutorId() {
        UUID tutorId = UUID.randomUUID();
        String userJson = buildUserJson(tutorId, "Sonia", "Trabelsi", "TUTOR");

        Course course = new Course();
        course.setCourseid(1);
        StudyGroup group = new StudyGroup();
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();

        when(userClient.getUserById(tutorId)).thenReturn(userJson);
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(mapper.toEntity(any(), any())).thenReturn(group);
        when(studyGroupRepository.save(group)).thenReturn(group);
        when(mapper.toDTO(group)).thenReturn(dto);

        studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId));

        // Vérifier l'argument exact passé à Feign
        verify(userClient).getUserById(eq(tutorId));
    }

    /**
     * Test 3 : Le JSON retourné par Feign est correctement désérialisé en UserDTO.
     * On vérifie que le nom du tuteur est bien extrait.
     */
    @Test
    void shouldDeserializeFeignResponseToUserDTO() throws Exception {
        UUID tutorId = UUID.randomUUID();
        String userJson = buildUserJson(tutorId, "Karim", "Mansour", "TUTOR");

        // Vérifier la désérialisation directement
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );
        UserDTO user = objectMapper.readValue(userJson, UserDTO.class);

        assertEquals("Karim",   user.getName());
        assertEquals("Mansour", user.getLastName());
        assertEquals("TUTOR",   user.getRole());
        assertEquals(tutorId,   user.getId());
    }

    // ═══════════════════════════════════════════════════════════
    // CAS D'ERREUR — CustomErrorDecoder & exceptions Feign
    // ═══════════════════════════════════════════════════════════

    /**
     * Test 4 : Le microservice User retourne 404 (tuteur introuvable).
     * Le CustomErrorDecoder lève ResourceNotFoundException.
     * → createStudyGroupWithValidation doit propager l'erreur.
     */
    @Test
    void shouldThrowWhenTutorNotFoundViaFeign() {
        UUID tutorId = UUID.randomUUID();

        // Simuler le comportement du CustomErrorDecoder pour HTTP 404
        when(userClient.getUserById(tutorId))
                .thenThrow(new ResourceNotFoundException(
                        "Le tuteur avec cet ID n'existe pas dans le système User."));

        StudyGroupRequestDTO request = buildRequest(tutorId);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(request));

        assertTrue(ex.getMessage().contains("Erreur de récupération du tuteur"));

        // Le groupe ne doit PAS être créé si le tuteur est introuvable
        verify(studyGroupRepository, never()).save(any());
    }

    /**
     * Test 5 : Le microservice User retourne 401 (non authentifié).
     * Le CustomErrorDecoder lève UnauthorizedException.
     */
    @Test
    void shouldThrowWhenFeignReturns401Unauthorized() {
        UUID tutorId = UUID.randomUUID();

        when(userClient.getUserById(tutorId))
                .thenThrow(new UnauthorizedException(
                        "Non authentifié pour appeler le service User."));

        assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId)));

        verify(studyGroupRepository, never()).save(any());
    }

    /**
     * Test 6 : Le microservice User retourne 403 (accès refusé).
     * Le CustomErrorDecoder lève UnauthorizedException.
     */
    @Test
    void shouldThrowWhenFeignReturns403Forbidden() {
        UUID tutorId = UUID.randomUUID();

        when(userClient.getUserById(tutorId))
                .thenThrow(new UnauthorizedException(
                        "Accès refusé au service User. Le token est manquant, expiré, ou n'a pas les droits ADMIN."));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId)));

        assertTrue(ex.getMessage().contains("Erreur de récupération du tuteur"));
        verify(studyGroupRepository, never()).save(any());
    }

    /**
     * Test 7 : Le microservice User est indisponible (timeout / connexion refusée).
     * Feign lève une RuntimeException générique.
     */
    @Test
    void shouldThrowWhenUserServiceIsUnavailable() {
        UUID tutorId = UUID.randomUUID();

        when(userClient.getUserById(tutorId))
                .thenThrow(new RuntimeException("Connection refused: User service is down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId)));

        assertNotNull(ex.getMessage());
        verify(studyGroupRepository, never()).save(any());
    }

    /**
     * Test 8 : Feign retourne null (réponse vide du service User).
     * → createStudyGroupWithValidation doit lever une exception.
     */
    @Test
    void shouldThrowWhenFeignReturnsNull() {
        UUID tutorId = UUID.randomUUID();

        // Feign retourne null → le JSON est null → exception lors du readValue
        when(userClient.getUserById(tutorId)).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId)));

        verify(studyGroupRepository, never()).save(any());
    }

    /**
     * Test 9 : Feign retourne un JSON malformé.
     * → La désérialisation échoue → exception propagée.
     */
    @Test
    void shouldThrowWhenFeignReturnsInvalidJson() {
        UUID tutorId = UUID.randomUUID();

        when(userClient.getUserById(tutorId)).thenReturn("NOT_VALID_JSON{{{");

        assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId)));

        verify(studyGroupRepository, never()).save(any());
    }

    /**
     * Test 10 : Feign est appelé UNE SEULE FOIS par création de groupe.
     * Vérification que le client n'est pas appelé plusieurs fois inutilement.
     */
    @Test
    void shouldCallFeignExactlyOnce() {
        UUID tutorId = UUID.randomUUID();
        String userJson = buildUserJson(tutorId, "Leila", "Hamdi", "TUTOR");

        Course course = new Course();
        course.setCourseid(1);
        StudyGroup group = new StudyGroup();
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();

        when(userClient.getUserById(tutorId)).thenReturn(userJson);
        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(mapper.toEntity(any(), any())).thenReturn(group);
        when(studyGroupRepository.save(group)).thenReturn(group);
        when(mapper.toDTO(group)).thenReturn(dto);

        studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId));

        // Exactement 1 appel Feign, pas plus
        verify(userClient, times(1)).getUserById(any());
    }

    /**
     * Test 11 : Si le tuteur est valide mais le cours est introuvable,
     * Feign est quand même appelé mais le groupe n'est pas créé.
     */
    @Test
    void shouldCallFeignButFailIfCourseNotFound() {
        UUID tutorId = UUID.randomUUID();
        String userJson = buildUserJson(tutorId, "Omar", "Khlifi", "TUTOR");

        when(userClient.getUserById(tutorId)).thenReturn(userJson);
        when(courseRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> studyGroupService.createStudyGroupWithValidation(buildRequest(tutorId)));

        // Feign a été appelé
        verify(userClient, times(1)).getUserById(tutorId);
        // Mais le groupe n'a pas été sauvegardé
        verify(studyGroupRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════
    // TEST CustomErrorDecoder en isolation
    // ═══════════════════════════════════════════════════════════

    /**
     * Test 12 : Vérifier que CustomErrorDecoder mappe correctement les codes HTTP.
     */
    @Test
    void shouldCustomErrorDecoderMap404ToResourceNotFoundException() {
        tn.spring.course.Config.CustomErrorDecoder decoder =
                new tn.spring.course.Config.CustomErrorDecoder();

        // Simuler une réponse Feign avec status 404
        feign.Response response404 = feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(feign.Request.create(
                        feign.Request.HttpMethod.GET,
                        "/api/users/test",
                        java.util.Collections.emptyMap(),
                        null,
                        null,
                        null
                ))
                .headers(java.util.Collections.emptyMap())
                .build();

        Exception ex = decoder.decode("UserClient#getUserById", response404);

        assertInstanceOf(ResourceNotFoundException.class, ex);
        assertTrue(ex.getMessage().contains("n'existe pas"));
    }

    /**
     * Test 13 : CustomErrorDecoder mappe 403 → UnauthorizedException.
     */
    @Test
    void shouldCustomErrorDecoderMap403ToUnauthorizedException() {
        tn.spring.course.Config.CustomErrorDecoder decoder =
                new tn.spring.course.Config.CustomErrorDecoder();

        feign.Response response403 = feign.Response.builder()
                .status(403)
                .reason("Forbidden")
                .request(feign.Request.create(
                        feign.Request.HttpMethod.GET,
                        "/api/users/test",
                        java.util.Collections.emptyMap(),
                        null,
                        null,
                        null
                ))
                .headers(java.util.Collections.emptyMap())
                .build();

        Exception ex = decoder.decode("UserClient#getUserById", response403);

        assertInstanceOf(UnauthorizedException.class, ex);
        assertTrue(ex.getMessage().contains("Accès refusé"));
    }

    /**
     * Test 14 : CustomErrorDecoder mappe 401 → UnauthorizedException.
     */
    @Test
    void shouldCustomErrorDecoderMap401ToUnauthorizedException() {
        tn.spring.course.Config.CustomErrorDecoder decoder =
                new tn.spring.course.Config.CustomErrorDecoder();

        feign.Response response401 = feign.Response.builder()
                .status(401)
                .reason("Unauthorized")
                .request(feign.Request.create(
                        feign.Request.HttpMethod.GET,
                        "/api/users/test",
                        java.util.Collections.emptyMap(),
                        null,
                        null,
                        null
                ))
                .headers(java.util.Collections.emptyMap())
                .build();

        Exception ex = decoder.decode("UserClient#getUserById", response401);

        assertInstanceOf(UnauthorizedException.class, ex);
        assertTrue(ex.getMessage().contains("Non authentifié"));
    }
}
