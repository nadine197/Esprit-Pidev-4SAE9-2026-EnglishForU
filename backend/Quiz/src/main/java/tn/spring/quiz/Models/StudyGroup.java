package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Date;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private String name;
    private String level;
    private String location;
    private int maxCapacity;

    private java.time.LocalDate startdate;
    private java.time.LocalDate enddate;

    @JsonBackReference(value = "studygroup-ref")
    @ManyToOne(fetch = FetchType.LAZY)
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