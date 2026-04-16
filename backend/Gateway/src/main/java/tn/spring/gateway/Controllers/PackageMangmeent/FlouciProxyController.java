package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

@RestController
@RequestMapping("/api/flouci")
public class FlouciProxyController {

    private final ProxyForwarder proxy;
    private static final String FLOUCI_BASE = "http://localhost:8085/api/flouci";

    public FlouciProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(FLOUCI_BASE + "/create", HttpMethod.POST, body, req);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String paymentId, HttpServletRequest req) {
        return proxy.forward(FLOUCI_BASE + "/verify?paymentId=" + paymentId, HttpMethod.GET, null, req);
    }
}