package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionProxyController {
    private final ProxyForwarder proxy;
    private final String discussionServiceBaseUrl;

    public DiscussionProxyController(ProxyForwarder proxy,
                                     @Value("${services.discussion.base-url:http://localhost:8088}") String discussionServiceBaseUrl) {
        this.proxy = proxy;
        this.discussionServiceBaseUrl = discussionServiceBaseUrl;
    }

    @GetMapping("/feed")
    public ResponseEntity<String> feed(@RequestParam(required = false) String scope,
                                       @RequestParam(required = false) String level,
                                       @RequestParam(required = false) String courseId,
                                       @RequestParam(required = false) String viewerLevel,
                                       HttpServletRequest req) {
        StringBuilder url = new StringBuilder(discussionServiceBaseUrl + "/api/discussions/feed");

        boolean hasQuery = false;
        hasQuery = appendQueryParam(url, hasQuery, "scope", scope);
        hasQuery = appendQueryParam(url, hasQuery, "level", level);
        hasQuery = appendQueryParam(url, hasQuery, "courseId", courseId);
        appendQueryParam(url, hasQuery, "viewerLevel", viewerLevel);

        return proxy.forward(url.toString(), HttpMethod.GET, null, req);
    }

    @PostMapping("/posts")
    public ResponseEntity<String> createPost(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(discussionServiceBaseUrl + "/api/discussions/posts", HttpMethod.POST, body, req);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<String> getPost(@PathVariable Long postId,
                                          @RequestParam(required = false) String viewerLevel,
                                          HttpServletRequest req) {
        String url = discussionServiceBaseUrl + "/api/discussions/posts/" + postId;
        if (viewerLevel != null && !viewerLevel.isBlank()) {
            url += "?viewerLevel=" + UriUtils.encode(viewerLevel, StandardCharsets.UTF_8);
        }
        return proxy.forward(url, HttpMethod.GET, null, req);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<String> addComment(@PathVariable Long postId,
                                             @RequestParam(required = false) String viewerLevel,
                                             @RequestBody Map<String, Object> body,
                                             HttpServletRequest req) {
        String url = discussionServiceBaseUrl + "/api/discussions/posts/" + postId + "/comments";
        if (viewerLevel != null && !viewerLevel.isBlank()) {
            url += "?viewerLevel=" + UriUtils.encode(viewerLevel, StandardCharsets.UTF_8);
        }
        return proxy.forward(url, HttpMethod.POST, body, req);
    }

    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<String> addReaction(@PathVariable Long postId,
                                              @RequestParam(required = false) String viewerLevel,
                                              @RequestBody Map<String, Object> body,
                                              HttpServletRequest req) {
        String url = discussionServiceBaseUrl + "/api/discussions/posts/" + postId + "/reactions";
        if (viewerLevel != null && !viewerLevel.isBlank()) {
            url += "?viewerLevel=" + UriUtils.encode(viewerLevel, StandardCharsets.UTF_8);
        }
        return proxy.forward(url, HttpMethod.POST, body, req);
    }

    @PostMapping(value = "/posts/{postId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@PathVariable Long postId,
                                              @RequestPart("file") MultipartFile file,
                                              HttpServletRequest req) {
        String url = discussionServiceBaseUrl + "/api/discussions/posts/" + postId + "/image";
        return proxy.forwardMultipart(url, HttpMethod.POST, "file", file, req);
    }

    @GetMapping(value = "/media/{fileName:.+}", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> media(@PathVariable String fileName, HttpServletRequest req) {
        String encodedFileName = UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
        String url = discussionServiceBaseUrl + "/api/discussions/media/" + encodedFileName;
        return proxy.forwardBytes(url, HttpMethod.GET, null, req);
    }

    private boolean appendQueryParam(StringBuilder url,
                                     boolean hasQuery,
                                     String key,
                                     String value) {
        if (value == null || value.isBlank()) {
            return hasQuery;
        }

        if (!hasQuery) {
            url.append('?');
        } else {
            url.append('&');
        }

        url.append(key)
                .append('=')
                .append(UriUtils.encode(value, StandardCharsets.UTF_8));

        return true;
    }
}
