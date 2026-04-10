package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        try {
            ResponseEntity<byte[]> response =
                    restTemplate.exchange(url, method, entity, byte[].class);

            HttpHeaders out = new HttpHeaders();
            out.putAll(response.getHeaders());
            return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            HttpHeaders out = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                out.putAll(ex.getResponseHeaders());
            }
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), out, ex.getStatusCode());
        } catch (Exception ex) {
            return new ResponseEntity<>(new byte[0], HttpStatus.BAD_GATEWAY);
        }
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

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, method, entity, String.class);

            HttpHeaders out = new HttpHeaders();
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                out.put(HttpHeaders.SET_COOKIE, cookies);
            }
            return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            HttpHeaders out = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                List<String> cookies = ex.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
                if (cookies != null) {
                    out.put(HttpHeaders.SET_COOKIE, cookies);
                }
            }
            return new ResponseEntity<>(ex.getResponseBodyAsString(), out, ex.getStatusCode());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"UPSTREAM_UNAVAILABLE\"}");
        }
    }

    public ResponseEntity<String> forwardMultipart(String url,
                                                   HttpMethod method,
                                                   String partName,
                                                   MultipartFile file,
                                                   HttpServletRequest req) {

        HttpHeaders headers = new HttpHeaders();

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) headers.set(HttpHeaders.AUTHORIZATION, auth);

        String cookie = req.getHeader(HttpHeaders.COOKIE);
        if (cookie != null) headers.set(HttpHeaders.COOKIE, cookie);

        String accept = req.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) headers.set(HttpHeaders.ACCEPT, accept);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            HttpHeaders filePartHeaders = new HttpHeaders();
            if (file.getContentType() != null && !file.getContentType().isBlank()) {
                filePartHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
            } else {
                filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, filePartHeaders);
            MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
            multipartBody.add(partName, filePart);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(multipartBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            HttpHeaders out = new HttpHeaders();
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                out.put(HttpHeaders.SET_COOKIE, cookies);
            }
            return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"INVALID_MULTIPART_PAYLOAD\"}");
        } catch (HttpStatusCodeException ex) {
            HttpHeaders out = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                List<String> cookies = ex.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
                if (cookies != null) {
                    out.put(HttpHeaders.SET_COOKIE, cookies);
                }
            }
            return new ResponseEntity<>(ex.getResponseBodyAsString(), out, ex.getStatusCode());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"UPSTREAM_UNAVAILABLE\"}");
        }
    }
}