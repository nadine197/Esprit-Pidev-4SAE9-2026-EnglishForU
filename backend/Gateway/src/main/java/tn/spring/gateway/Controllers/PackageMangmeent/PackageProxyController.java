package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class PackageProxyController {

    private final ProxyForwarder proxy;
    private static final String PACKAGE_BASE = "http://localhost:8085/api/packages";

    public PackageProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE, HttpMethod.POST, body, req);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<String> addItem(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/" + id + "/items", HttpMethod.POST, body, req);
    }

    @GetMapping("/active")
    public ResponseEntity<String> active(HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/active", HttpMethod.GET, null, req);
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String q, HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/search?q=" + q, HttpMethod.GET, null, req);
    }
}