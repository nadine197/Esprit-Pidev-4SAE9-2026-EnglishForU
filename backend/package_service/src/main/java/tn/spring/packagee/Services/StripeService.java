package tn.spring.packagee.Services;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.StripeCheckoutRequest;
import tn.spring.packagee.Enum.PaymentMethod;

@Service
public class StripeService {
    private final PaymentService paymentService;

    public StripeService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public String createCheckoutSession(StripeCheckoutRequest request) throws Exception {

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)

                        // ✅ PASS paymentId in URL
                        .setSuccessUrl("http://localhost:4200/payment-result?session_id={CHECKOUT_SESSION_ID}&status=success&id=" + request.getPaymentId())
                        .setCancelUrl("http://localhost:4200/payment-result?status=failed&id=" + request.getPaymentId())

                        // ✅ ALSO store in metadata (VERY IMPORTANT)
                        .putMetadata("paymentId", String.valueOf(request.getPaymentId()))

                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(request.getAmount() * 100L)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(request.getPackageName())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        Session session = Session.create(params);
        System.out.println("✅ Payment update: " + request.getPaymentId());

        ConfirmPaymentRequest cofn = new ConfirmPaymentRequest();
        cofn.setProvider(PaymentMethod.STRIPE);
        cofn.setProviderRef(session.getId());

        paymentService.confirm(request.getPaymentId(), cofn);

        System.out.println("✅ Payment confirmed: " + request.getPaymentId());
        return session.getUrl();
    }
}