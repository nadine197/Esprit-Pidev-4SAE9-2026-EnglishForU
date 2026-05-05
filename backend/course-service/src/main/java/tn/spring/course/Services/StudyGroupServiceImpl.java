package tn.spring.course.Services;
import tn.spring.course.Clients.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Mappers.StudyGroupMapper;
import tn.spring.course.Models.Course;
import tn.spring.course.
        Models.StudyGroup;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Repositories.StudyGroupRepository;
import tn.spring.course.Models.StudyGroupStatus;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tn.spring.course.DTO.UserDTO;
@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;
    private final StudyGroupMapper mapper;
    private final UserClient userClient;
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${google.maps.api.key}")
    private String mapsApiKey;
    @Value("${google.maps.geocode.url}")
    private String geocodeUrl;
    @Override
    public StudyGroupResponseDTO createStudyGroup(StudyGroupRequestDTO dto) {

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        StudyGroup group = mapper.toEntity(dto, course);

        return mapper.toDTO(studyGroupRepository.save(group));
    }

    @Override
    public StudyGroupResponseDTO updateStudyGroup(Long id, StudyGroupRequestDTO dto) {

        StudyGroup existing = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyGroup not found"));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        existing.setName(dto.getName());
        existing.setLevel(dto.getLevel());
        existing.setLocation(dto.getLocation());
        existing.setMaxCapacity(dto.getMaxCapacity());
        existing.setStartdate(dto.getStartdate());
        existing.setEnddate(dto.getEnddate());
        existing.setStatus(dto.getStatus());
        existing.setCourse(course);
        existing.setTutorId(dto.getTutorId());
        existing.setStudentsIds(dto.getStudentsIds());

        return mapper.toDTO(studyGroupRepository.save(existing));
    }

    @Override
    public StudyGroupResponseDTO getStudyGroup(Long id) {
        return mapper.toDTO(
                studyGroupRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("StudyGroup not found"))
        );
    }

    @Override
    public List<StudyGroupResponseDTO> getAllStudyGroups() {
        return studyGroupRepository.findAllWithStudents()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStudyGroup(Long id) {
        studyGroupRepository.deleteById(id);
    }
    public List<StudyGroupResponseDTO> getGroupsByDate(Date clickedDate) {
        // Normaliser la date (ignorer heures/min/sec)
        Date normalized = stripTime(clickedDate);

        return studyGroupRepository.findByDate(normalized)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
    public List<StudyGroupResponseDTO> getGroupsByMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();

        // Premier jour du mois
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();

        // Dernier jour du mois
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfMonth = cal.getTime();

        return studyGroupRepository.findByMonthRange(startOfMonth, endOfMonth)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // Dates marquées du mois (pour afficher les points sur le calendrier)
    public Map<String, List<String>> getMarkedDates(int year, int month) {
        List<StudyGroupResponseDTO> groups = getGroupsByMonth(year, month);
        Map<String, List<String>> marked = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (StudyGroupResponseDTO g : groups) {
            // Marquer startdate
            String start = sdf.format(g.getStartdate());
            marked.computeIfAbsent(start, k -> new ArrayList<>())
                    .add("start:" + g.getName());

            // Marquer enddate
            String end = sdf.format(g.getEnddate());
            marked.computeIfAbsent(end, k -> new ArrayList<>())
                    .add("end:" + g.getName());
        }
        return marked;
    }

    private Date stripTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private StudyGroupResponseDTO toResponseDTO(StudyGroup g) {
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
        dto.setGroupId(g.getGroupId());
        dto.setName(g.getName());
        dto.setLevel(g.getLevel());
        dto.setLocation(g.getLocation());
        dto.setMaxCapacity(g.getMaxCapacity());
        dto.setStartdate(g.getStartdate());
        dto.setEnddate(g.getEnddate());
        dto.setStatus(g.getStatus());
        dto.setCourseId(g.getCourse() != null ? g.getCourse().getCourseid() : null);
        dto.setTutorId(g.getTutorId());
        dto.setStudentsIds(g.getStudentsIds());
        return dto;
    }
    @Override
    public Map<String, Object> getStats() {
        List<StudyGroup> all = studyGroupRepository.findAllWithStudents();

        // ── KPIs globaux ──────────────────────────────────────────
        long totalGroups = all.size();

        long activeGroups = all.stream()
                .filter(g -> "ACTIVE".equals(String.valueOf(g.getStatus())))
                .count();

        long totalStudents = all.stream()
                .mapToLong(g -> g.getStudentsIds() != null ? g.getStudentsIds().size() : 0)
                .sum();

        double avgFill = all.stream()
                .filter(g -> g.getMaxCapacity() > 0)
                .mapToDouble(g -> {
                    int enrolled = g.getStudentsIds() != null ? g.getStudentsIds().size() : 0;
                    return (enrolled * 100.0) / g.getMaxCapacity();
                })
                .average().orElse(0.0);

        // ── Par statut ────────────────────────────────────────────
        Map<String, Long> countByStatus = new LinkedHashMap<>();
        studyGroupRepository.countByStatus()
                .forEach(row -> countByStatus.put(
                        String.valueOf(row[0]), (Long) row[1]
                ));

        // ── Par niveau ────────────────────────────────────────────
        Map<String, Long> countByLevel = new LinkedHashMap<>();
        studyGroupRepository.countByLevel()
                .forEach(row -> countByLevel.put(
                        String.valueOf(row[0]), (Long) row[1]
                ));

        // ── Taux de remplissage par niveau ────────────────────────
        Map<String, Double> fillRateByLevel = new LinkedHashMap<>();
        studyGroupRepository.avgFillRateByLevel()
                .forEach(row -> fillRateByLevel.put(
                        String.valueOf(row[0]),
                        row[1] != null ? Math.round((Double) row[1] * 10.0) / 10.0 : 0.0
                ));

        // ── Capacité vs inscrits par niveau ───────────────────────
        Map<String, Map<String, Object>> capacityByLevel = new LinkedHashMap<>();
        studyGroupRepository.capacityVsEnrolledByLevel()
                .forEach(row -> {
                    long maxCap   = ((Number) row[1]).longValue();
                    long enrolled = ((Number) row[2]).longValue();
                    double pct    = maxCap > 0
                            ? Math.round((enrolled * 100.0 / maxCap) * 10.0) / 10.0
                            : 0.0;

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("maxCapacity", maxCap);
                    entry.put("enrolled",    enrolled);
                    entry.put("fillPercent", pct);
                    capacityByLevel.put(String.valueOf(row[0]), entry);
                });

        // ── Par mois ──────────────────────────────────────────────
        Map<String, Long> countByMonth = new LinkedHashMap<>();
        studyGroupRepository.countByMonth()
                .forEach(row -> countByMonth.put(
                        String.valueOf(row[0]), (Long) row[1]
                ));

        // ── Top 5 groupes les plus remplis ────────────────────────
        List<StudyGroupResponseDTO> topGroups = studyGroupRepository
                .findTopByFillRate(PageRequest.of(0, 5))
                .stream()
                .map(this::toResponseDTO)
                .toList();

        // ── Assemblage final ──────────────────────────────────────
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalGroups",     totalGroups);
        stats.put("activeGroups",    activeGroups);
        stats.put("totalStudents",   totalStudents);
        stats.put("avgFillRate",     Math.round(avgFill * 10.0) / 10.0);
        stats.put("countByStatus",   countByStatus);
        stats.put("countByLevel",    countByLevel);
        stats.put("fillRateByLevel", fillRateByLevel);
        stats.put("capacityByLevel", capacityByLevel);
        stats.put("countByMonth",    countByMonth);
        stats.put("topGroups",       topGroups);

        return stats;
    }
    @Override
    public List<StudyGroupResponseDTO> searchGroups(
            String name, String level, String status,
            String location, Integer courseId) {

        List<StudyGroup> all = studyGroupRepository.findAllWithStudents();

        // 2. Filtrer en Java
        return all.stream()
                .filter(g -> name == null || name.isBlank() ||
                        g.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(g -> level == null || level.isBlank() ||
                        level.equalsIgnoreCase(g.getLevel()))
                .filter(g -> status == null || status.isBlank() ||
                        status.equalsIgnoreCase(String.valueOf(g.getStatus())))
                .filter(g -> location == null || location.isBlank() ||
                        g.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(g -> courseId == null ||
                        (g.getCourse() != null &&
                                courseId.equals(g.getCourse().getCourseid())))
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    public List<Map<String, Object>> getAuditLog(Long groupId) {

        Revisions<Long, StudyGroup> revisions =
                studyGroupRepository.findRevisions(groupId);

        return revisions.stream().map(rev -> {
            StudyGroup g        = rev.getEntity();
            Map<String, Object> entry = new LinkedHashMap<>();

            entry.put("revision",  rev.getRevisionNumber().orElse(null));
            entry.put("type",      rev.getMetadata().getRevisionType().name()); // INSERT UPDATE DELETE
            entry.put("timestamp", rev.getRevisionInstant()
                    .map(Object::toString).orElse(""));
            entry.put("groupId",   g.getGroupId());
            entry.put("name",      g.getName());
            entry.put("level",     g.getLevel());
            entry.put("location",  g.getLocation());
            entry.put("status",    g.getStatus() != null ? g.getStatus().name() : null);
            entry.put("maxCapacity", g.getMaxCapacity());
            entry.put("startdate", g.getStartdate() != null ? g.getStartdate().toString() : null);
            entry.put("enddate",   g.getEnddate()   != null ? g.getEnddate().toString()   : null);

            return entry;
        }).toList();
    }
    @Override
    public String chat(String message, Long groupId) {
        String msg = message.toLowerCase();

        // ── 1. Tentative de réponse locale (Intent detection simple) pour économiser le quota ──
        if (groupId != null && (
                msg.contains("niveau") || msg.contains("quand") || msg.contains("où") ||
                msg.contains("lieu") || msg.contains("place") || msg.contains("capacité") ||
                msg.contains("date") || msg.contains("commence") || msg.contains("statut")
        )) {
            return generateFallbackResponse(message, groupId);
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append(
                "Tu es un assistant pour une plateforme d'école de langue anglaise. " +
                        "Tu réponds aux questions sur les groupes d'étude. " +
                        "Réponds toujours en français de manière claire et concise.\n\n"
        );

        if (groupId != null) {
            studyGroupRepository.findById(groupId).ifPresent(g -> {
                prompt.append("Contexte du groupe :\n")
                        .append("- Nom : ").append(g.getName()).append("\n")
                        .append("- Niveau : ").append(g.getLevel()).append("\n")
                        .append("- Localisation : ").append(g.getLocation()).append("\n")
                        .append("- Capacité max : ").append(g.getMaxCapacity()).append("\n")
                        .append("- Statut : ").append(g.getStatus()).append("\n")
                        .append("- Date début : ").append(g.getStartdate()).append("\n")
                        .append("- Date fin : ").append(g.getEnddate()).append("\n\n");
            });
        }

        prompt.append("Question : ").append(message);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt.toString())))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 500
                )
        );

        try {
            Map response = WebClient.builder().build()
                    .post()
                    .uri(geminiApiUrl + "?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map> candidates = (List<Map>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return generateFallbackResponse(message, groupId);
            }
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (WebClientResponseException e) {
            System.out.println("GEMINI STATUS: " + e.getStatusCode().value());
            if (e.getStatusCode().value() == 429) {
                // ✅ Fallback automatique si quota dépassé
                return generateFallbackResponse(message, groupId);
            }
            return "Désolé, je rencontre une difficulté technique (Erreur " + e.getStatusCode().value() + ").";
        } catch (Exception e) {
            System.out.println("GEMINI EXCEPTION: " + e.getMessage());
            return generateFallbackResponse(message, groupId);
        }
    }

    private String generateFallbackResponse(String message, Long groupId) {
        String msg = message.toLowerCase();

        if (groupId == null) {
            List<StudyGroup> all = studyGroupRepository.findAll();
            if (all.isEmpty()) return "Il n'y a actuellement aucun groupe d'étude disponible.";

            if (msg.contains("niveau")) {
                Set<String> levels = all.stream().map(StudyGroup::getLevel).collect(Collectors.toSet());
                return "Nous proposons des groupes pour les niveaux suivants : " + String.join(", ", levels) + ".";
            }
            if (msg.contains("où") || msg.contains("lieu") || msg.contains("localisation")) {
                Set<String> locations = all.stream().map(StudyGroup::getLocation).collect(Collectors.toSet());
                return "Nos groupes se réunissent dans divers lieux : " + String.join(", ", locations) + ".";
            }
            if (msg.contains("combien") || msg.contains("nombre") || msg.contains("groupe")) {
                return "Il y a actuellement " + all.size() + " groupes d'étude actifs sur la plateforme.";
            }

            return "Je suis l'assistant Study Group. Il y a " + all.size() +
                   " groupes disponibles. Posez-moi une question sur les niveaux, les lieux ou sélectionnez un groupe spécifique.";
        }

        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) return "Désolé, je ne trouve pas les détails de ce groupe.";

        StudyGroup g = groupOpt.get();

        if (msg.contains("niveau")) {
            return "Le groupe d'étude '" + g.getName() + "' est de niveau " + g.getLevel() + ".";
        }
        if (msg.contains("où") || msg.contains("lieu") || msg.contains("emplacement") || msg.contains("localisation")) {
            return "Les sessions se déroulent à : " + g.getLocation() + ".";
        }
        if (msg.contains("quand") || msg.contains("date") || msg.contains("commence") || msg.contains("début")) {
            return "Le groupe est prévu du " + g.getStartdate() + " au " + g.getEnddate() + ".";
        }
        if (msg.contains("place") || msg.contains("capacité") || msg.contains("combien") || msg.contains("inscrit")) {
            int enrolled = g.getStudentsIds() != null ? g.getStudentsIds().size() : 0;
            int remaining = g.getMaxCapacity() - enrolled;
            return "La capacité est de " + g.getMaxCapacity() + " places. Il y a " + enrolled + " inscrits et " +
                   (remaining > 0 ? remaining + " places restantes." : "le groupe est complet.");
        }
        if (msg.contains("statut") || msg.contains("état")) {
            return "Le statut actuel de ce groupe est : " + g.getStatus() + ".";
        }

        // Réponse par défaut informative
        return "Le groupe '" + g.getName() + "' est de niveau " + g.getLevel() +
               ", situé à " + g.getLocation() + ", et son statut est " + g.getStatus() + ".";
    }

    @Override
    public StudyGroupResponseDTO createStudyGroupWithValidation(
            StudyGroupRequestDTO dto) {

        // ── Vérifier que le tuteur existe via le microservice User ──
        try {
            String tutorJson = userClient.getUserById(dto.getTutorId());
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            UserDTO tutor = mapper.readValue(tutorJson, UserDTO.class);
            
            if (tutor == null) {
                throw new RuntimeException("Tuteur introuvable avec l'ID : " + dto.getTutorId());
            }
            System.out.println("Tuteur vérifié : " + tutor.getName() + " " + tutor.getLastName());
        } catch (Exception e) {
            throw new RuntimeException("Erreur de récupération du tuteur: " + e.getMessage(), e);
        }

        // ── Créer le groupe si le tuteur est valide ─────────────────
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        StudyGroup group = mapper.toEntity(dto, course);
        return mapper.toDTO(studyGroupRepository.save(group));
    }

}
