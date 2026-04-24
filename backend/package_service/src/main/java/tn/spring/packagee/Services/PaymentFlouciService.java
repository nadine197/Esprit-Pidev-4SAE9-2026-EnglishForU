package tn.spring.packagee.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.spring.packagee.DTOs.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentFlouciService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${flouci.public-key}")
    private String publicKey;

    @Value("${flouci.private-key}")
    private String privateKey;

    @Value("${flouci.developer-tracking-prefix}")
    private String developerTrackingId;

    @Value("${app.frontend-url}") // front url
    private String baseUrl;

    public ResponsePaymentFlouciDTO generatePayment(BigInteger amountMillimes) throws IOException {

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amountMillimes); // ✅ millimes
        payload.put("success_link", baseUrl + "/payment-callback?status=success");
        payload.put("fail_link", baseUrl + "/payment-callback?status=failed");
        payload.put("developer_tracking_id", developerTrackingId);

        // optional:
        // payload.put("accept_card", true);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url("https://developers.flouci.com/api/v2/generate_payment") // ✅ v2
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + publicKey + ":" + privateKey) // ✅ NEW AUTH
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Flouci generate_payment failed. HTTP=" + response.code() + " body=" + responseBody);
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode resultNode = jsonNode.path("result");

            return ResponsePaymentFlouciDTO.builder()
                    .payment_id(resultNode.path("payment_id").asText())
                    .link(resultNode.path("link").asText())
                    .build();
        }
    }

    public boolean verifyPayment(String paymentId) throws IOException {
        Request request = new Request.Builder()
                .url("https://developers.flouci.com/api/v2/verify_payment/" + paymentId) // ✅ v2
                .get()
                .addHeader("Authorization", "Bearer " + publicKey + ":" + privateKey) // ✅ NEW AUTH
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Flouci verify_payment failed. HTTP=" + response.code() + " body=" + responseBody);
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String status = jsonNode.path("result").path("status").asText();
            return "SUCCESS".equalsIgnoreCase(status);
        }
    }
}