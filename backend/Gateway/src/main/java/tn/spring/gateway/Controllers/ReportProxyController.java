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
}
