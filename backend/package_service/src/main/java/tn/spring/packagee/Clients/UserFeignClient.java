package tn.spring.packagee.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.spring.packagee.Config.FeignConfig;
import tn.spring.packagee.DTOs.UserPublicDTO;

/**
 * Feign client vers le microservice User (lb://User via Eureka).
 * Remplace le RestTemplate hardcodé sur localhost:8081.
 */
@FeignClient(name = "User", configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/api/users/public/by-email")
    UserPublicDTO getPublicByEmail(@RequestParam("email") String email);
}
