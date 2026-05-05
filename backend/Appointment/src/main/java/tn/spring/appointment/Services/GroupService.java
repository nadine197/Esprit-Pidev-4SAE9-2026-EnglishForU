package tn.spring.appointment.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.appointment.Models.DiscussionGroup;
import tn.spring.appointment.Repositories.GroupRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

    public DiscussionGroup createGroup(DiscussionGroup group) {
        group.setCreatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    public List<DiscussionGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    public void deleteGroup(UUID id) {
        groupRepository.deleteById(id);
    }
}
