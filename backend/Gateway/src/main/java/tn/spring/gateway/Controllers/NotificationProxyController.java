package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationProxyController {

    private final ProxyForwarder proxy;
    private final String userServiceBaseUrl;

    public NotificationProxyController(ProxyForwarder proxy,
                                       @Value("${services.user.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        this.proxy = proxy;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @GetMapping
    public ResponseEntity<String> list(HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/notifications", HttpMethod.GET, null, req);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<String> unreadCount(HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/notifications/unread-count", HttpMethod.GET, null, req);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<String> markRead(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/notifications/" + id + "/read", HttpMethod.POST, null, req);
    }
}
