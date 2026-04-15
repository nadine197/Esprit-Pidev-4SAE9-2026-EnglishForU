package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseid;

    @org.hibernate.annotations.UuidGenerator
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    private String title;
    private String description;
    private Integer duration;
    private Long creator;

    // ================= CONTENT =================
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<Content> contents;

    // ================= STUDY GROUP =================
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<StudyGroup> studyGroups;

    // ================= QUIZ =================
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<Quiz> quizzes;
}