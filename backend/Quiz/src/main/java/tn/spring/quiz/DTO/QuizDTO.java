package tn.spring.quiz.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caracteres")
    private String title;

    @NotNull(message = "L'identifiant du cours est obligatoire")
    private Long courseId;

    public QuizDTO(Long id, String title, UUID id1) {
    }
}