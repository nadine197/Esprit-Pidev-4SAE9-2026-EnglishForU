package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportProxyController {

    private final ProxyForwarder proxy;
    private final String userServiceBaseUrl;

    public ReportProxyController(ProxyForwarder proxy,
                                 @Value("${services.user.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        this.proxy = proxy;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/reports", HttpMethod.POST, body, req);
    }

    @GetMapping("/mine")
    public ResponseEntity<String> mine(HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/reports/mine", HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> byId(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/reports/" + id, HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<String> listComments(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/reports/" + id + "/comments", HttpMethod.GET, null, req);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<String> addComment(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/reports/" + id + "/comments", HttpMethod.POST, body, req);
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<String> activity(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/reports/" + id + "/activity", HttpMethod.GET, null, req);
    }
}
