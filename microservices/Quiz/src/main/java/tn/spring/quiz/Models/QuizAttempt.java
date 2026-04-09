package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore

    private Quiz quiz;

    private Integer score;
    private Boolean passed;

    private LocalDateTime submittedAt;
}
