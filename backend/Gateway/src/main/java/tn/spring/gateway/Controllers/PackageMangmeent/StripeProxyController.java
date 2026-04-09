package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

@RestController
@RequestMapping("/api/stripe")
public class StripeProxyController {

    private final ProxyForwarder proxy;
    private final String packageServiceBaseUrl;

    public StripeProxyController(ProxyForwarder proxy,
                                 @Value("${services.package.base-url:http://localhost:8085}") String packageServiceBaseUrl) {
        this.proxy = proxy;
        this.packageServiceBaseUrl = packageServiceBaseUrl;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckout(
            @RequestBody Object body,   HttpServletRequest req) {

        return proxy.forward(
            packageServiceBaseUrl + "/api/stripe/create-checkout-session" ,
                HttpMethod.POST,
                body,
                req
        );
    }
}