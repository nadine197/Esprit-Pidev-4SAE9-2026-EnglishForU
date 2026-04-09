package tn.spring.appointment.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.appointment.Models.DiscussionGroup;
import tn.spring.appointment.Repositories.GroupRepository;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GroupController {

    private final GroupRepository repository;

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
}