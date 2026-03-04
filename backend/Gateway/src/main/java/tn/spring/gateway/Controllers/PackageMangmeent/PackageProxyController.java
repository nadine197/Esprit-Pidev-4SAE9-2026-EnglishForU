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

    // ✅ CREATE package (ADMIN)
    @PostMapping
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE, HttpMethod.POST, body, req);
    }

    // ✅ GET ALL packages (ADMIN)  ---> NEW
    @GetMapping
    public ResponseEntity<String> all(HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE, HttpMethod.GET, null, req);
    }

    // ✅ UPDATE package (ADMIN) ---> NEW
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody Object body,
                                         HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/" + id, HttpMethod.PUT, body, req);
    }

    // ✅ DISABLE package (ADMIN) ---> NEW
    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/" + id + "/disable", HttpMethod.PUT, null, req);
    }

    // ✅ ENABLE package (ADMIN) ---> NEW
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/" + id + "/enable", HttpMethod.PUT, null, req);
    }

    // ✅ ADD ITEM (ADMIN)
    @PostMapping("/{id}/items")
    public ResponseEntity<String> addItem(@PathVariable Long id,
                                          @RequestBody Object body,
                                          HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/" + id + "/items", HttpMethod.POST, body, req);
    }

    // ✅ LIST ACTIVE (STUDENT/ADMIN)
    @GetMapping("/active")
    public ResponseEntity<String> active(HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/active", HttpMethod.GET, null, req);
    }

    // ✅ SEARCH (STUDENT/ADMIN)
    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String q, HttpServletRequest req) {
        return proxy.forward(PACKAGE_BASE + "/search?q=" + q, HttpMethod.GET, null, req);
    }
}