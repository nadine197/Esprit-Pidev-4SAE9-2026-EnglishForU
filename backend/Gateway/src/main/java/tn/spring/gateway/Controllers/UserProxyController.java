package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserProxyController {

    private final ProxyForwarder proxy;
    private final String userServiceBaseUrl;

    public UserProxyController(ProxyForwarder proxy,
                               @Value("${services.user.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        this.proxy = proxy;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @PostMapping("/create-employee")
    public ResponseEntity<String> createEmployee(@RequestBody Map<String, Object> body,
                                                 HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/create-employee", HttpMethod.POST, body, req);
    }

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/create-user", HttpMethod.POST, body, req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id,
                                         @RequestBody Map<String, Object> body,
                                         HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/" + id, HttpMethod.PUT, body, req);
    }

    @PutMapping("/block/{id}")
    public ResponseEntity<String> block(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/block/" + id, HttpMethod.PUT, null, req);
    }

    @PutMapping("/unblock/{id}")
    public ResponseEntity<String> unblock(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/unblock/" + id, HttpMethod.PUT, null, req);
    }

    @PutMapping("/role/{id}")
    public ResponseEntity<String> changeRole(@PathVariable String id,
                                             @RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/role/" + id, HttpMethod.PUT, body, req);
    }

    @GetMapping("/admins")
    public ResponseEntity<String> getAdmins(HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/admins", HttpMethod.GET, null, req);
    }

    @GetMapping("/students")
    public ResponseEntity<String> getStudents(HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/students", HttpMethod.GET, null, req);
    }

    @GetMapping("/tutors")
    public ResponseEntity<String> getTutors(HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/tutors", HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/" + id, HttpMethod.GET, null, req);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<String> getUsersByRole(@PathVariable String role, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/users/role/" + role, HttpMethod.GET, null, req);
    }
}