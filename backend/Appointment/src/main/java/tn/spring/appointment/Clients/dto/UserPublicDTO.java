package tn.spring.appointment.Clients.dto;

import lombok.Data;
import java.util.UUID;

/**
 * DTO minimal reçu depuis le microservice User.
 */
@Data
public class UserPublicDTO {
    private UUID id;
    private String name;
    private String lastName;
}
