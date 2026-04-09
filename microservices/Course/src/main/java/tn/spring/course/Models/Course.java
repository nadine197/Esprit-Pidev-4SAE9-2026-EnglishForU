package tn.spring.course.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseid;

    private String title;
    private String description;
    private Integer duration;

    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    private UUID adminId;

    @JsonManagedReference
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Content> contents;

    @JsonManagedReference(value="studygroup-ref")
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<StudyGroup> studyGroups;
}