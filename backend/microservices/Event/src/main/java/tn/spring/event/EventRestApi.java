package tn.spring.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mic2/events")
public class EventRestApi {
    @GetMapping("/hello")
    public String sayhello(){
        return "Hello Im microservice2";
    }
}

