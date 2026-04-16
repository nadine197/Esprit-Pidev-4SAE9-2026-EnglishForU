package tn.spring.packagee.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.packagee.Clients.UserFeignClient;
import tn.spring.packagee.DTOs.UserPublicDTO;

import java.util.UUID;

/**
 * Délègue les appels vers le microservice User via Feign (lb://User).
 * Remplace l'ancien RestTemplate hardcodé sur localhost:8081.
 */
@Service
@RequiredArgsConstructor
public class UserClient {

    private final UserFeignClient userFeignClient;

    public UserPublicDTO fetchStudentByEmail(String email) {
        return userFeignClient.getPublicByEmail(email);
    }
}