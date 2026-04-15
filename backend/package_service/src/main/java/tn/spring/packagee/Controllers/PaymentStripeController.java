package tn.spring.packagee.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.StripeCheckoutRequest;
import tn.spring.packagee.Services.StripeService;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin("*") // optional but useful
public class PaymentStripeController {

    private final StripeService stripeService;

    public PaymentStripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT')")
    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(
            @RequestBody StripeCheckoutRequest request

            ) throws Exception {
        return Map.of(
                "url", stripeService.createCheckoutSession(request)
        );
    }
}
