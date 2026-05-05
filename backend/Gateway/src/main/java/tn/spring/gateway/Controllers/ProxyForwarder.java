package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ProxyForwarder {

    private final RestTemplate restTemplate;

    public ProxyForwarder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public ResponseEntity<byte[]> forwardBytes(String url,
                                               HttpMethod method,
                                               Object body,
                                               HttpServletRequest req) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        String userId = (String) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");
        System.out.println("from proxy " + role + userId);

        if (userId != null) headers.set("X-User-Id", userId);
        if (role != null) headers.set("X-User-Role", role);
        if (auth != null) headers.set(HttpHeaders.AUTHORIZATION, auth);

        String accept = req.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) headers.set(HttpHeaders.ACCEPT, accept);

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response =
                restTemplate.exchange(url, method, entity, byte[].class);

        HttpHeaders out = new HttpHeaders();
        out.putAll(response.getHeaders());
        return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
    }
    public ResponseEntity<String> forward(String url,
                                          HttpMethod method,
                                          Object body,
                                          HttpServletRequest req) {

        HttpHeaders headers = new HttpHeaders();

        String userId = (String) req.getAttribute("userId");
        String role   = (String) req.getAttribute("role");

        if (userId != null) headers.set("X-User-Id", userId);
        if (role != null) headers.set("X-User-Role", role);

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) headers.set(HttpHeaders.AUTHORIZATION, auth);

        String cookie = req.getHeader(HttpHeaders.COOKIE);
        if (cookie != null) headers.set(HttpHeaders.COOKIE, cookie);

        String accept = req.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) headers.set(HttpHeaders.ACCEPT, accept);

        String contentType = req.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType != null) {
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        } else if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, method, entity, String.class);

        // IMPORTANT FIX
        HttpHeaders out = new HttpHeaders();
        out.putAll(response.getHeaders());

        return new ResponseEntity<>(
                response.getBody(),
                out,
                response.getStatusCode()
        );
    }
}