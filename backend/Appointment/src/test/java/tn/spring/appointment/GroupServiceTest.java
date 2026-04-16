package tn.spring.appointment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.appointment.Models.DiscussionGroup;
import tn.spring.appointment.Repositories.GroupRepository;
import tn.spring.appointment.Services.GroupService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @InjectMocks
    private GroupService groupService;

    @Test
    @DisplayName("CRUD : Création de groupe avec timestamp")
    void testCreateGroup() {
        DiscussionGroup group = new DiscussionGroup();
        group.setGroupName("English Class A");
        when(groupRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DiscussionGroup created = groupService.createGroup(group);

        assertNotNull(created.getCreatedAt());
        assertEquals("English Class A", created.getGroupName());
        verify(groupRepository).save(any());
    }
}
