package tn.spring.discussion.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.spring.discussion.clients.dto.UserPublicDTO;
import tn.spring.discussion.config.FeignConfig;

/**
 * Feign client vers le microservice User (lb://User via Eureka).
 * Permet d'enrichir les posts/commentaires avec le nom complet de l'auteur.
 */
@FeignClient(name = "User", configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/api/users/public/by-email")
    UserPublicDTO getPublicByEmail(@RequestParam("email") String email);
}
