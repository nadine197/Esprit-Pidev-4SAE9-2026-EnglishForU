package tn.spring.user.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tn.spring.user.Enums.AvailableStatus;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table
@SuperBuilder
public class Tutor extends User {


    private Boolean verified;
    @Enumerated(EnumType.STRING)
    private AvailableStatus availability_status;
    private int experience_years;
    private int rating;
}
