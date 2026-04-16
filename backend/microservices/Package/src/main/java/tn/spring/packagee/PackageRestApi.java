package tn.spring.packagee;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mic5/packages")
public class PackageRestApi {
    @GetMapping("/hello")
    public String sayhello(){
        return "Hello im microservice5 ";
    }
}

