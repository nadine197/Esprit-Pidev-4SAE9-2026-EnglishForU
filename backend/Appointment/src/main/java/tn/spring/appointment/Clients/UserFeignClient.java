package tn.spring.appointment.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.spring.appointment.Config.FeignConfig;
import tn.spring.appointment.Clients.dto.UserPublicDTO;

/**
 * Feign client vers le microservice User.
 * Utilisé pour enrichir les notifications avec le nom complet de l'utilisateur.
 */
@FeignClient(name = "User", configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/api/users/public/by-email")
    UserPublicDTO getPublicByEmail(@RequestParam("email") String email);
}
