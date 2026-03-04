package tn.spring.appointment.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class DiscussionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String groupName;
    private String tutorId;
    private String tutorName;

    @ElementCollection
    private List<String> studentIds;

    private LocalDateTime createdAt;
}