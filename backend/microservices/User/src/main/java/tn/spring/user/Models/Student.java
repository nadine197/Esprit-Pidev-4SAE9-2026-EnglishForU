package tn.spring.user.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table
@SuperBuilder
public class Student extends User  {
    private String englishLevel;
    private String learningGoal;
    private Integer dailyGoalMinutes;
}

