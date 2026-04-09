package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

@RestController
@RequestMapping("/api/payments")
public class PaymentProxyController {

    private final ProxyForwarder proxy;
    private final String packageServiceBaseUrl;

    public PaymentProxyController(ProxyForwarder proxy,
                                  @Value("${services.package.base-url:http://localhost:8085}") String packageServiceBaseUrl) {
        this.proxy = proxy;
        this.packageServiceBaseUrl = packageServiceBaseUrl;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Object body, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments", HttpMethod.POST, body, req);
    }
    @GetMapping
    public ResponseEntity<String> list(
                                       HttpServletRequest req) {


        return proxy.forward(packageServiceBaseUrl + "/api/payments", HttpMethod.GET, null, req);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id,
                                               @RequestParam String status,
                                               HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments/" + id + "/status?status=" + status, HttpMethod.POST, null, req);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments/" + id, HttpMethod.DELETE, null, req);
    }
    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirm(@PathVariable Long id,
                                          @RequestBody Object body,
                                          HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments/" + id + "/confirm", HttpMethod.POST, body, req);
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<String> fail(@PathVariable Long id,

                                       HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments/" + id + "/fail?reason=", HttpMethod.POST, null, req);
    }

    @GetMapping(value = "/{id}/voucher", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> voucher(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forwardBytes(packageServiceBaseUrl + "/api/payments/" + id + "/voucher", HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments/" + id, HttpMethod.GET, null, req);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<String> byStudent(@PathVariable Long studentId, HttpServletRequest req) {
        return proxy.forward(packageServiceBaseUrl + "/api/payments/student/" + studentId, HttpMethod.GET, null, req);
    }
}