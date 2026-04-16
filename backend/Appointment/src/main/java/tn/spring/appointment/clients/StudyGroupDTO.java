package tn.spring.appointment.clients;

import lombok.Data;
import java.util.List;

@Data
public class StudyGroupDTO {
    private String id;
    private String name;
    private String tutorEmail;
    private String tutorName;
    private List<String> studentEmails; // On utilise toujours les emails !
}