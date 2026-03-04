package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

import java.util.Map;

@RestController
@RequestMapping("/api/promos")
public class PromoProxyController {

    private final ProxyForwarder proxy;
    private static final String PROMO_BASE = "http://localhost:8085/api/promos";

    public PromoProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE, HttpMethod.POST, body, req);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/validate", HttpMethod.POST, body, req);
    }
}