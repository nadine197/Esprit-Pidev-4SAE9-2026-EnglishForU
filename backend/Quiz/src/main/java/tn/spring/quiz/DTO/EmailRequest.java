package tn.spring.quiz.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {
    private String email;
    private String subject;
    private String corp;

}