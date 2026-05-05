package tn.spring.quiz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mic/quiz")
public class QuizRestApi {

    @GetMapping("/hello")
    public String sayhello(){
        return "Hello im microservice Quiz";
    }
}
