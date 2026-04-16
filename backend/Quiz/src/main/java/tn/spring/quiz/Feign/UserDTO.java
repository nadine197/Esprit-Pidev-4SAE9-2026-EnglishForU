package tn.spring.quiz.Feign;

import lombok.Data;
import tn.spring.quiz.Enums.UserRole;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private String englishLevel;
    private String learningGoal;
    private Integer dailyGoalMinutes;
}