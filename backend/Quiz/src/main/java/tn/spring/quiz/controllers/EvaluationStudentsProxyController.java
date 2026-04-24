package tn.spring.quiz.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.quiz.Feign.UserDTO;

import java.util.List;

/**
 * Proxies the student list from the User service with the browser's JWT forwarded.
 * Uses {@link RestTemplate} so auth forwarding does not depend on Feign interceptor wiring.
 * Path is {@code /api/evaluations/students} (not under {@code /api/evaluations/{id}}) to avoid
 * Spring treating {@code "students"} as a numeric id.
 */
@CrossOrigin(origins = {"http://localhost:4200", "*", "*"})
@RestController
@RequestMapping("/api/evaluations/students")
public class EvaluationStudentsProxyController {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public EvaluationStudentsProxyController(
            RestTemplate restTemplate,
            @Value("${quiz.services.user-url:http://localhost:8081}") String userServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceUrl.replaceAll("/+$", "");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<List<UserDTO>> listStudentsForEvaluation(HttpServletRequest request) {
        String url = userServiceBaseUrl + "/api/users/students";
        HttpHeaders headers = new HttpHeaders();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<UserDTO>>() {}
            );
            List<UserDTO> body = response.getBody();
            return ResponseEntity.ok(body != null ? body : List.of());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), ex.getMessage(), ex);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cannot reach User service at " + userServiceBaseUrl + ". Start the User microservice (default port 8081) or set quiz.services.user-url.",
                    ex
            );
        }
    }
}
