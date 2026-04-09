package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/helpdesk/reports")
public class HelpdeskReportProxyController {

    private final ProxyForwarder proxy;
    private final String userServiceBaseUrl;

    public HelpdeskReportProxyController(ProxyForwarder proxy,
                                         @Value("${services.user.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        this.proxy = proxy;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @GetMapping
    public ResponseEntity<String> list(@RequestParam(required = false) String status, HttpServletRequest req) {
        String url = userServiceBaseUrl + "/api/helpdesk/reports";
        if (status != null && !status.isBlank()) {
            url += "?status=" + UriUtils.encode(status, StandardCharsets.UTF_8);
        }
        return proxy.forward(url, HttpMethod.GET, null, req);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         HttpServletRequest req) {
        return proxy.forward(userServiceBaseUrl + "/api/helpdesk/reports/" + id, HttpMethod.PATCH, body, req);
    }
}
