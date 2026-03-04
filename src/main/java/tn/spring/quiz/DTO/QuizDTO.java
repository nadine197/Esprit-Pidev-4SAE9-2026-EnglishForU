package tn.spring.quiz.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizDTO {

    private Long id;
    private String title;
    private Long courseId;

    public QuizDTO(Long id, String title, UUID id1) {
    }
}