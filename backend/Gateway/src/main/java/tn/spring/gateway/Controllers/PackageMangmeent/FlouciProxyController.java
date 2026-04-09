package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

@RestController
@RequestMapping("/api/flouci")
public class FlouciProxyController {

    private final ProxyForwarder proxy;
    private final String packageServiceBaseUrl;

    public FlouciProxyController(ProxyForwarder proxy,
                                 @Value("${services.package.base-url:http://localhost:8085}") String packageServiceBaseUrl) {
        this.proxy = proxy;
        this.packageServiceBaseUrl = packageServiceBaseUrl;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/flouci/create", HttpMethod.POST, body, req);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String paymentId, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/flouci/verify?paymentId=" + paymentId, HttpMethod.GET, null, req);
    }
}