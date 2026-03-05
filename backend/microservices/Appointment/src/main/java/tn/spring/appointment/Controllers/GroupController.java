package tn.spring.appointment.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.appointment.Models.DiscussionGroup;
import tn.spring.appointment.Repositories.GroupRepository;
import tn.spring.appointment.Services.GroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class GroupController {
    private final GroupRepository repository;

    @GetMapping("/groups/user/{userId}")
    public List<DiscussionGroup> getMyGroups(@PathVariable String userId) {
        System.out.println("RECHERCHE DE GROUPES POUR L'ID : [" + userId + "]"); // <--- LOG CRUCIAL
        List<DiscussionGroup> groups = repository.findGroupsByMemberId(userId);
        System.out.println("NOMBRE DE GROUPES TROUVÉS : " + groups.size());
        return groups;
    }

    @GetMapping("/groups/all") // Pour l'admin
    public List<DiscussionGroup> getAll() {
        return repository.findAll();
    }

    @PutMapping("/groups/{id}")
    public DiscussionGroup update(@PathVariable UUID id, @RequestBody DiscussionGroup groupDetails) {
        DiscussionGroup group = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setGroupName(groupDetails.getGroupName());
        group.setTutorId(groupDetails.getTutorId());
        group.setTutorName(groupDetails.getTutorName());
        group.setStudentIds(groupDetails.getStudentIds());

        return repository.save(group);
    }

    @DeleteMapping("/groups/{id}")
    public void delete(@PathVariable UUID id) {
        repository.deleteById(id);
    }

    @PostMapping("/groups")
    public DiscussionGroup create(@RequestBody DiscussionGroup group) {
        return repository.save(group);
    }
}