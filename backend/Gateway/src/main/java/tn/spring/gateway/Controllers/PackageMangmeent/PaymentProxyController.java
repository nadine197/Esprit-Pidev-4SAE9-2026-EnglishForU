package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentProxyController {

    private final ProxyForwarder proxy;
    private static final String PAY_BASE = "http://localhost:8085/api/payments";

    public PaymentProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(PAY_BASE, HttpMethod.POST, body, req);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirm(@PathVariable Long id,
                                          @RequestBody Object body,
                                          HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id + "/confirm", HttpMethod.POST, body, req);
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<String> fail(@PathVariable Long id,
                                       @RequestParam String reason,
                                       HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id + "/fail?reason=" + reason, HttpMethod.POST, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<String> byStudent(@PathVariable Long studentId, HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/student/" + studentId, HttpMethod.GET, null, req);
    }
}