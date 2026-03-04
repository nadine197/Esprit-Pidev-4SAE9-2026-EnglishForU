package tn.spring.appointment.Models;

import jakarta.persistence.*;
import lombok.*;
import tn.spring.appointment.Enums.ApptStatus;
import tn.spring.appointment.Enums.LocationType; // Import de l'Enum créé à l'étape 1
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String visitorName;
    private String visitorEmail;
    private String visitorPhone;
    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    private ApptStatus status;

    private String levelResult;

    // --- Nouveaux champs ajoutés ---

    private String accessCode; // Le code envoyé par email pour accéder au test

    @Enumerated(EnumType.STRING)
    private LocationType locationType; // Choix : ON_SITE ou REMOTE

    private String qcmScore; // Pour stocker "3/4" par exemple

    private int tabSwitchCount; // Nombre de fois où il a changé d'onglet

}