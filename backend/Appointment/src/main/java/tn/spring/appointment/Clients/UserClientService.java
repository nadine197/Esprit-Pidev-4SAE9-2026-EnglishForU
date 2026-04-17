package tn.spring.appointment.Clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.spring.appointment.Clients.dto.UserPublicDTO;

/**
 * Wrapper autour du Feign client User.
 * Gère les erreurs gracieusement : si le service User est indisponible,
 * retourne null sans faire planter le service Appointment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserClientService {

    private final UserFeignClient userFeignClient;

    /**
     * Récupère les infos publiques d'un utilisateur par email.
     * Retourne null si le service User est indisponible.
     */
    public UserPublicDTO getUserByEmail(String email) {
        try {
            return userFeignClient.getPublicByEmail(email);
        } catch (Exception e) {
            log.warn("User service unavailable for email {}: {}", email, e.getMessage());
            return null;
        }
    }
}
