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

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
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

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) headers.set(HttpHeaders.AUTHORIZATION, auth);

        String cookie = req.getHeader(HttpHeaders.COOKIE);
        if (cookie != null) headers.set(HttpHeaders.COOKIE, cookie);

        String accept = req.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) headers.set(HttpHeaders.ACCEPT, accept);

        String contentType = req.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType != null) headers.set(HttpHeaders.CONTENT_TYPE, contentType);

        if (body != null && headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, method, entity, String.class);

        HttpHeaders out = new HttpHeaders();
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            out.put(HttpHeaders.SET_COOKIE, cookies);
        }
        return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
    }
}