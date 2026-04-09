package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;


@RestController
@RequestMapping("/api/promos")
public class PromoProxyController {

    private final ProxyForwarder proxy;
    private final String packageServiceBaseUrl;

    public PromoProxyController(ProxyForwarder proxy,
                                @Value("${services.package.base-url:http://localhost:8085}") String packageServiceBaseUrl) {
        this.proxy = proxy;
        this.packageServiceBaseUrl = packageServiceBaseUrl;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos", HttpMethod.POST, body, req);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos/validate", HttpMethod.POST, body, req);
    }
    @GetMapping
    public ResponseEntity<String> getAll(HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos", HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos/" + id, HttpMethod.GET, null, req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody Object body,
                                         HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos/" + id, HttpMethod.DELETE, null, req);
    }
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos/" + id + "/enable", HttpMethod.PUT, null, req);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disable(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/promos/" + id + "/disable", HttpMethod.PUT, null, req);
    }
}