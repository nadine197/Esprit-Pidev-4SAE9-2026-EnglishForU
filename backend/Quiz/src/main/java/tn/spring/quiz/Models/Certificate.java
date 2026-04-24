package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID studentId;

    private String studentName;

    private String studentEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Course course;

    private Integer finalScore;

    private LocalDate issueDate;
}
