package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionProxyController {
    private final ProxyForwarder proxy;
    private final String discussionBaseUrl;

    public DiscussionProxyController(
            ProxyForwarder proxy,
            @Value("${services.discussion.base-url:http://localhost:8088}") String discussionBaseUrl) {
        this.proxy = proxy;
        this.discussionBaseUrl = discussionBaseUrl;
    }

    private String discussionApiBase() {
        return discussionBaseUrl + "/api/discussions";
    }

    @PostMapping("/groups")
    public ResponseEntity<String> createGroup(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups", HttpMethod.POST, body, req);
    }

    @GetMapping("/groups/user/{userId}")
    public ResponseEntity<String> getMyGroups(@PathVariable String userId, HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups/user/" + userId, HttpMethod.GET, null, req);
    }

    @GetMapping("/groups/all")
    public ResponseEntity<String> getAllGroups(HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups/all", HttpMethod.GET, null, req);
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<String> updateGroup(@PathVariable String id, @RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups/" + id, HttpMethod.DELETE, null, req);
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<String> getHistory(@PathVariable String groupId, HttpServletRequest req) {
        String backendUrl = discussionApiBase() + "/groups/" + groupId + "/messages";
        return proxy.forward(backendUrl, HttpMethod.GET, null, req);
    }
}
