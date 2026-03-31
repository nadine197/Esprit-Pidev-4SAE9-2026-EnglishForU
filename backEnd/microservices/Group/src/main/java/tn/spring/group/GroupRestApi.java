package tn.spring.group;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mic3/groups")
public class GroupRestApi {
    @GetMapping("/hello")
    public String sayhello(){
        return "Hello im microservice3";
    }
}

