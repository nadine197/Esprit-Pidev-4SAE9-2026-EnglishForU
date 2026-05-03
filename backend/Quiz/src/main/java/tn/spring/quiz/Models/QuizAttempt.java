package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID studentId;

    private String studentName;

    private String studentEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore

    private Quiz quiz;

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuizAttemptAnswer> attemptAnswers = new ArrayList<>();

    private Integer score;
    private Boolean passed;

    private LocalDateTime submittedAt;
}
