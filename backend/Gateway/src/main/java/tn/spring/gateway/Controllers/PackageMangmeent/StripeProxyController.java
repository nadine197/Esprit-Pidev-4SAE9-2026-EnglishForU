package tn.spring.gateway.Controllers.PackageMangmeent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.gateway.Controllers.ProxyForwarder;

@RestController
@RequestMapping("/api/stripe")
public class StripeProxyController {

    private final ProxyForwarder proxy;
    private static final String STRIPE_BASE = "http://localhost:8085/api/stripe";

    public StripeProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckout(
            @RequestBody Object body,   HttpServletRequest req) {

        return proxy.forward(
                STRIPE_BASE + "/create-checkout-session" ,
                HttpMethod.POST,
                body,
                req
        );
    }
}