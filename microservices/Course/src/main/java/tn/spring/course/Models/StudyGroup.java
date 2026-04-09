package tn.spring.course.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;
import java.util.Date;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID tutorId;
    @ElementCollection
    @CollectionTable(
            name = "group_students",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @Column(name = "student_id", columnDefinition = "uuid")
    private List<UUID> studentsIds;

}