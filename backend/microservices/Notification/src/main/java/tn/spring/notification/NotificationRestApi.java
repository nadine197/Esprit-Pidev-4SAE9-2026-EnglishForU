package tn.spring.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mic6/notifications")
public class NotificationRestApi {
    @GetMapping("/hello")
    public String sayhello(){
        return "Hello im microservice5s";
    }
}

