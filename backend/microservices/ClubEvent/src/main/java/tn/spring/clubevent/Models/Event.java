package tn.spring.clubevent.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    private LocalDateTime eventDate;

    private String location;

    @Column(nullable = false)
    private Long clubId;

    private String clubName;

    private LocalDateTime createdAt;

    private Double latitude;
    private Double longitude;
    private String locationName;

    private boolean paid = false;
    private Double price;
    private Integer maxParticipants;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}

