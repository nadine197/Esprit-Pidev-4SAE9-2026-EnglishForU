package tn.spring.gateway.Controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

/**
 * Proxy controller that forwards requests from the Gateway
 * to the ClubEvent microservice (lb://CLUBEVENT).
 *
 * Routes covered:
 *  - /api/clubs/**
 *  - /api/events/**
 *  - /api/feedback/**
 *  - /api/images/**
 */
@RestController
public class ClubEventProxyController {

    private final WebClient webClient;

    public ClubEventProxyController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("lb://CLUBEVENT")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generic proxy method – forwards any HTTP method + body + headers
    // ─────────────────────────────────────────────────────────────────────────

    private Mono<ResponseEntity<byte[]>> proxy(ServerHttpRequest request, String targetPath) {
        HttpMethod method = request.getMethod();
        HttpHeaders incomingHeaders = request.getHeaders();

        // Build the full URI with query string
        String query = request.getURI().getRawQuery();
        String uri = query != null ? targetPath + "?" + query : targetPath;

        WebClient.RequestBodySpec requestSpec = webClient
                .method(method)
                .uri(uri)
                .headers(headers -> {
                    incomingHeaders.forEach((name, values) -> {
                        // Skip hop-by-hop headers
                        if (!name.equalsIgnoreCase(HttpHeaders.HOST) &&
                            !name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING) &&
                            !name.equalsIgnoreCase(HttpHeaders.CONNECTION)) {
                            headers.addAll(name, values);
                        }
                    });
                });

        return requestSpec
                .body(BodyInserters.fromDataBuffers(request.getBody()))
                .retrieve()
                .toEntity(byte[].class)
                .onErrorResume(ex -> Mono.just(
                        ResponseEntity.status(502)
                                .body(("ClubEvent service unavailable: " + ex.getMessage()).getBytes())
                ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // /api/clubs/**
    // ─────────────────────────────────────────────────────────────────────────

    @RequestMapping(value = {"/proxy/clubs", "/proxy/clubs/**"})
    public Mono<ResponseEntity<byte[]>> proxyClubs(ServerHttpRequest request) {
        String path = request.getURI().getPath().replaceFirst("^/proxy", "/api");
        return proxy(request, path);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // /api/events/**
    // ─────────────────────────────────────────────────────────────────────────

    @RequestMapping(value = {"/proxy/events", "/proxy/events/**"})
    public Mono<ResponseEntity<byte[]>> proxyEvents(ServerHttpRequest request) {
        String path = request.getURI().getPath().replaceFirst("^/proxy", "/api");
        return proxy(request, path);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // /api/feedback/**
    // ─────────────────────────────────────────────────────────────────────────

    @RequestMapping(value = {"/proxy/feedback", "/proxy/feedback/**"})
    public Mono<ResponseEntity<byte[]>> proxyFeedback(ServerHttpRequest request) {
        String path = request.getURI().getPath().replaceFirst("^/proxy", "/api");
        return proxy(request, path);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // /api/images/**
    // ─────────────────────────────────────────────────────────────────────────

    @RequestMapping(value = {"/proxy/images", "/proxy/images/**"})
    public Mono<ResponseEntity<byte[]>> proxyImages(ServerHttpRequest request) {
        String path = request.getURI().getPath().replaceFirst("^/proxy", "/api");
        return proxy(request, path);
    }
}
