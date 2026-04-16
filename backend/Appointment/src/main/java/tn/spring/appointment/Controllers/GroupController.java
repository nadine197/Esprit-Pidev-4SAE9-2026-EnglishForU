package tn.spring.appointment.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.appointment.Models.DiscussionGroup;
import tn.spring.appointment.Repositories.GroupRepository;
import tn.spring.appointment.clients.CourseClient;
import tn.spring.appointment.clients.StudyGroupDTO;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GroupController {

    private final GroupRepository repository;
    private final CourseClient courseClient; // Injecte le client créé à l'étape 2

    // --- CETTE MÉTHODE EST CELLE UTILISÉE PAR SAMIRA ET LES TUTEURS ---
    @GetMapping("/groups/user/{email}")
    public ResponseEntity<List<DiscussionGroup>> getMyGroups(@PathVariable String email) {
        System.out.println("Recherche des groupes pour l'email : " + email);

        // On utilise la méthode qui existe dans le Repository
        List<DiscussionGroup> groups = repository.findByTutorEmailOrStudentEmailsContaining(email, email);

        System.out.println("Groupes trouvés : " + groups.size());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/all")
    public List<DiscussionGroup> getAll() {
        return repository.findAll();
    }

    @PostMapping("/groups")
    public DiscussionGroup create(@RequestBody DiscussionGroup group) {
        group.setCreatedAt(LocalDateTime.now());
        return repository.save(group);
    }

    @PutMapping("/groups/{id}")
    public DiscussionGroup update(@PathVariable UUID id, @RequestBody DiscussionGroup groupDetails) {
        DiscussionGroup group = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setGroupName(groupDetails.getGroupName());
        group.setTutorEmail(groupDetails.getTutorEmail());
        group.setTutorName(groupDetails.getTutorName());
        group.setStudentEmails(groupDetails.getStudentEmails());

        return repository.save(group);
    }

    @DeleteMapping("/groups/{id}")
    public void delete(@PathVariable UUID id) {
        repository.deleteById(id);
    }

    @PostMapping("/groups/from-study-group/{studyGroupId}")
    public ResponseEntity<DiscussionGroup> createFromStudyGroup(@PathVariable String studyGroupId) {

        // 1. On appelle le microservice Course pour récupérer les détails du groupe d'étude
        StudyGroupDTO studyGroup = courseClient.getStudyGroupById(studyGroupId);

        // 2. On crée la discussion automatiquement avec les données reçues
        DiscussionGroup discussionGroup = DiscussionGroup.builder()
                .groupName(studyGroup.getName()) // Même nom
                .tutorEmail(studyGroup.getTutorEmail()) // Même tuteur
                .tutorName(studyGroup.getTutorName())
                .studentEmails(studyGroup.getStudentEmails()) // Mêmes étudiants
                .createdAt(LocalDateTime.now())
                .build();

        // 3. On sauvegarde dans PostgreSQL (Appointment DB)
        return ResponseEntity.ok(repository.save(discussionGroup));
    }
}