package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    @GetMapping
    public ResponseEntity<String> list(
                                       HttpServletRequest req) {


        return proxy.forward(PAY_BASE, HttpMethod.GET, null, req);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id,
                                               @RequestParam String status,
                                               HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id + "/status?status=" + status, HttpMethod.POST, null, req);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id, HttpMethod.DELETE, null, req);
    }
    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirm(@PathVariable Long id,
                                          @RequestBody Object body,
                                          HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id + "/confirm", HttpMethod.POST, body, req);
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<String> fail(@PathVariable Long id,

                                       HttpServletRequest req) {
        return proxy.forward(PAY_BASE + "/" + id + "/fail?reason=", HttpMethod.POST, null, req);
    }

    @GetMapping(value = "/{id}/voucher", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> voucher(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forwardBytes(PAY_BASE + "/" + id + "/voucher", HttpMethod.GET, null, req);
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