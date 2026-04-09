package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;


@RestController
@RequestMapping("/api/promos")
public class PromoProxyController {

    private final ProxyForwarder proxy;
    private static final String PROMO_BASE = "http://localhost:8085/api/promos";

    public PromoProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE, HttpMethod.POST, body, req);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/validate", HttpMethod.POST, body, req);
    }
    @GetMapping
    public ResponseEntity<String> getAll(HttpServletRequest req) {
        return proxy.forward(PROMO_BASE, HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody Object body,
                                         HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/" + id, HttpMethod.DELETE, null, req);
    }
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/" + id + "/enable", HttpMethod.PUT, null, req);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PROMO_BASE + "/" + id + "/disable", HttpMethod.PUT, null, req);
    }
}