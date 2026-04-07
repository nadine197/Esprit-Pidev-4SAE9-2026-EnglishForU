package tn.spring.course.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

import tn.spring.course.Config.FeignConfig;

@FeignClient(name = "User", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    String getUserById(@PathVariable("id") UUID id);
}