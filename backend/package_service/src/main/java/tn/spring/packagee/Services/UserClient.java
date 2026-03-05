package tn.spring.packagee.Services;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.spring.packagee.DTOs.UserPublicDTO;


import java.util.UUID;

@Service
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchStudentFullName(UUID studentId) {
        String url = "http://localhost:8081/api/users/" + studentId + "/public";
        UserPublicDTO dto = restTemplate.getForObject(url, UserPublicDTO.class);
        if (dto == null) return null;
        return (dto.getLastName() + " " + dto.getName()).trim();
    }
}