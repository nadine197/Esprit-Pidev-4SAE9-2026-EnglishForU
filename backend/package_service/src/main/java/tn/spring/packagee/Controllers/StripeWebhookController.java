package tn.spring.packagee.Controllers;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.Enum.PaymentMethod;
import tn.spring.packagee.Services.PaymentService;

@RestController
@RequestMapping("/api/webhook")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;
    private final PaymentService paymentService;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public String handleStripeEvent(@RequestBody String payload,
                                    @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return "Invalid signature";
        }

        // Handle event
        if ("checkout.session.completed".equals(event.getType())) {

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            StripeObject stripeObject;

            if (deserializer.getObject().isPresent()) {
                stripeObject = deserializer.getObject().get();
            }else{
                stripeObject = null;
            }

            if (stripeObject instanceof Session session) {

                String paymentIdStr = session.getMetadata().get("paymentId");

                if (paymentIdStr == null) {
                    System.out.println("❌ paymentId missing in metadata");
                    return "Missing metadata";
                }

                Long paymentId = Long.parseLong(paymentIdStr);

                System.out.println("✅ Payment update: " + paymentId);

                ConfirmPaymentRequest cofn = new ConfirmPaymentRequest();
                cofn.setProvider(PaymentMethod.STRIPE);
                cofn.setProviderRef(session.getId());

                paymentService.confirm(paymentId, cofn);

                System.out.println("✅ Payment confirmed: " + paymentId);
            }
        }

        return "Success";
    }
}