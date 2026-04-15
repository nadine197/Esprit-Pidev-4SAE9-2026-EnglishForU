package tn.spring.packagee.Services;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tn.spring.packagee.DTOs.UserPublicDTO;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserPublicDTO fetchStudentByEmail(String email) {
        String url = "http://localhost:8081/api/users/public/by-email?email={email}";
        return restTemplate.getForObject(url, UserPublicDTO.class, email);
    }

}