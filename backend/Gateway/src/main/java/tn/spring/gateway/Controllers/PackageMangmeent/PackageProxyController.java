package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class PackageProxyController {

    private final ProxyForwarder proxy;
    private final String packageServiceBaseUrl;

    public PackageProxyController(ProxyForwarder proxy,
                                  @Value("${services.package.base-url:http://localhost:8085}") String packageServiceBaseUrl) {
        this.proxy = proxy;
        this.packageServiceBaseUrl = packageServiceBaseUrl;
    }

    // ✅ CREATE package (ADMIN)
    @PostMapping
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages", HttpMethod.POST, body, req);
    }

    // ✅ GET ALL packages (ADMIN)  ---> NEW
    @GetMapping
    public ResponseEntity<String> all(HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages", HttpMethod.GET, null, req);
    }
    @GetMapping("/{id}")
    public ResponseEntity<String> getbyId(@PathVariable Long id,
                                         HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/" + id, HttpMethod.GET, null, req);
    }
    // ✅ UPDATE package (ADMIN) ---> NEW
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody Object body,
                                         HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/" + id, HttpMethod.PUT, body, req);
    }

    // ✅ DISABLE package (ADMIN) ---> NEW
    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/" + id + "/disable", HttpMethod.PUT, null, req);
    }

    // ✅ ENABLE package (ADMIN) ---> NEW
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/" + id + "/enable", HttpMethod.PUT, null, req);
    }

    // ✅ ADD ITEM (ADMIN)
    @PostMapping("/{id}/items")
    public ResponseEntity<String> addItem(@PathVariable Long id,
                                          @RequestBody Object body,
                                          HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/" + id + "/items", HttpMethod.POST, body, req);
    }

    // ✅ LIST ACTIVE (STUDENT/ADMIN)
    @GetMapping("/active")
    public ResponseEntity<String> active(HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/active", HttpMethod.GET, null, req);
    }

    // ✅ SEARCH (STUDENT/ADMIN)
    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String q, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/packages/search?q=" + q, HttpMethod.GET, null, req);
    }
}