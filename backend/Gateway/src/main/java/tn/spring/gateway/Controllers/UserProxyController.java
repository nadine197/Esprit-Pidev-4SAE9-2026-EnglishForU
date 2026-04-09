package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserProxyController {

    private final ProxyForwarder proxy;

    public UserProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    private static final String USER_USERS_BASE = "http://localhost:8081/api/users";

    @PostMapping("/create-employee")
    public ResponseEntity<String> createEmployee(@RequestBody Map<String, Object> body,
                                                 HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/create-employee", HttpMethod.POST, body, req);
    }

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/create-user", HttpMethod.POST, body, req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id,
                                         @RequestBody Map<String, Object> body,
                                         HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/" + id, HttpMethod.PUT, body, req);
    }

    @PutMapping("/block/{id}")
    public ResponseEntity<String> block(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/block/" + id, HttpMethod.PUT, null, req);
    }

    @PutMapping("/unblock/{id}")
    public ResponseEntity<String> unblock(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/unblock/" + id, HttpMethod.PUT, null, req);
    }

    @PutMapping("/role/{id}")
    public ResponseEntity<String> changeRole(@PathVariable String id,
                                             @RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/role/" + id, HttpMethod.PUT, body, req);
    }

    @GetMapping("/admins")
    public ResponseEntity<String> getAdmins(HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/admins", HttpMethod.GET, null, req);
    }

    @GetMapping("/students")
    public ResponseEntity<String> getStudents(HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/students", HttpMethod.GET, null, req);
    }

    @GetMapping("/tutors")
    public ResponseEntity<String> getTutors(HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/tutors", HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<String> getUsersByRole(@PathVariable String role, HttpServletRequest req) {
        return proxy.forward(USER_USERS_BASE + "/role/" + role, HttpMethod.GET, null, req);
    }
}