package tn.spring.course;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mic4/courses")
public class CourseRestApi {
    @GetMapping("/hello")
    public String sayhello(){
        return "Hello im microservice4 ";
    }
}
