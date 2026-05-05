package tn.spring.course.Models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.swing.*;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited

public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private String name;
    private String level;
    private String location;
    private int maxCapacity;
    private Date startdate;
    private Date enddate;
    @Enumerated(EnumType.STRING)
    private StudyGroupStatus status;
    @JsonBackReference("studygroup-ref")
    @ManyToOne
    @JoinColumn(name = "course_id")
    @NotAudited
    private Course course;
    @Column(nullable = false, columnDefinition = "uuid")
    @NotAudited
    private UUID tutorId;
    @ElementCollection
    @CollectionTable(
            name = "group_students",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @Column(name = "student_id", columnDefinition = "uuid")
    @NotAudited
    private List<UUID> studentsIds;

}