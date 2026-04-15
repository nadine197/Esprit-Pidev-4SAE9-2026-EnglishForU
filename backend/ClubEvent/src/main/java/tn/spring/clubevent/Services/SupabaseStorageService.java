package tn.spring.clubevent.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.bucket:ticket-passes}")
    private String bucket;

    @PostConstruct
    public void ensureBucketExists() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String checkUrl = supabaseUrl + "/storage/v1/bucket/" + bucket;
            HttpRequest checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create(checkUrl))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .GET()
                    .build();

            HttpResponse<String> checkResponse = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());

            if (checkResponse.statusCode() == 404 || checkResponse.statusCode() == 400) {
                String body = "{\"id\":\"" + bucket + "\",\"name\":\"" + bucket + "\",\"public\":true}";
                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(supabaseUrl + "/storage/v1/bucket"))
                        .header("Authorization", "Bearer " + serviceRoleKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

                if (createResponse.statusCode() < 300) {
                    System.out.println("[Supabase] Bucket '" + bucket + "' created successfully.");
                    ensurePublicPolicy(client);
                } else if (createResponse.statusCode() == 409) {
                    System.out.println("[Supabase] Bucket '" + bucket + "' already exists.");
                } else {
                    System.err.println("[Supabase] Failed to create bucket: " + createResponse.statusCode() + " " + createResponse.body());
                }
            } else {
                System.out.println("[Supabase] Bucket '" + bucket + "' already exists.");
                ensurePublicPolicy(client);
            }
        } catch (Exception e) {
            System.err.println("[Supabase] Could not ensure bucket exists: " + e.getMessage());
        }
    }

    private void ensurePublicPolicy(HttpClient client) throws Exception {
        String[] policies = {
            "{\"name\":\"allow-public-read\",\"definition\":\"true\",\"action\":\"SELECT\",\"roles\":[\"anon\",\"authenticated\"]}",
            "{\"name\":\"allow-service-insert\",\"definition\":\"true\",\"action\":\"INSERT\",\"roles\":[\"service_role\"]}",
            "{\"name\":\"allow-service-update\",\"definition\":\"true\",\"action\":\"UPDATE\",\"roles\":[\"service_role\"]}"
        };

        for (String policy : policies) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/bucket/" + bucket + "/policies"))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(policy))
                    .build();
            client.send(req, HttpResponse.BodyHandlers.ofString());
        }
    }

    public String uploadPdf(byte[] pdfBytes, String fileName) throws Exception {
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", "application/pdf")
                .header("x-upsert", "true")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(pdfBytes))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Supabase upload failed (" + response.statusCode() + "): " + response.body());
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }
}
