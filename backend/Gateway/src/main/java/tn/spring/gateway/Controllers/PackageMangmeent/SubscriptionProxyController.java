package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionProxyController {

    private final ProxyForwarder proxy;
    private static final String SUB_BASE = "http://localhost:8085/api/subscriptions";

    public SubscriptionProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(SUB_BASE, HttpMethod.POST, body, req);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<String> byStudent(@PathVariable Long studentId, HttpServletRequest req) {
        return proxy.forward(SUB_BASE + "/student/" + studentId, HttpMethod.GET, null, req);
    }
}