package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentProxyController {

    private final ProxyForwarder proxy;
    private final String appointmentBaseUrl;
    private final String discussionBaseUrl;

    public AppointmentProxyController(
            ProxyForwarder proxy,
            @Value("${services.appointment.base-url:http://localhost:8087}") String appointmentBaseUrl,
            @Value("${services.discussion.base-url:http://localhost:8088}") String discussionBaseUrl) {
        this.proxy = proxy;
        this.appointmentBaseUrl = appointmentBaseUrl;
        this.discussionBaseUrl = discussionBaseUrl;
    }

    private String appointmentApiBase() {
        return appointmentBaseUrl + "/api/appointments";
    }

    private String discussionApiBase() {
        return discussionBaseUrl + "/api/discussions";
    }

    @PostMapping("/book")
    public ResponseEntity<String> book(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/book", HttpMethod.POST, body, req);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<String> getSlots(HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/available-slots", HttpMethod.GET, null, req);
    }

    @PostMapping("/slots")
    public ResponseEntity<String> addSlot(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/slots", HttpMethod.POST, body, req);
    }

    @GetMapping("/all")
    public ResponseEntity<String> getAll(HttpServletRequest req) {
        String queryString = req.getQueryString() != null ? "?" + req.getQueryString() : "";
        return proxy.forward(appointmentApiBase() + "/all" + queryString, HttpMethod.GET, null, req);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<String> accept(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/" + id + "/accept", HttpMethod.PUT, null, req);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/" + id + "/cancel", HttpMethod.PUT, null, req);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<String> reschedule(@PathVariable String id, @RequestParam String newDate, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/" + id + "/reschedule?newDate=" + newDate, HttpMethod.PUT, null, req);
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<String> validate(@PathVariable String code, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/validate/" + code, HttpMethod.GET, null, req);
    }

    @PostMapping("/verify-access")
    public ResponseEntity<String> verifyAccess(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentApiBase() + "/verify-access", HttpMethod.POST, body, req);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<String> complete(
            @PathVariable String id,
            @RequestParam String result,
            @RequestParam String score,
            @RequestParam int cheatCount,
            HttpServletRequest req) {

        String url = appointmentApiBase() + "/" + id + "/complete" +
                "?result=" + result +
                "&score=" + score +
                "&cheatCount=" + cheatCount;

        url = url.replace(" ", "%20");
        return proxy.forward(url, HttpMethod.PUT, null, req);
    }

    @PostMapping("/groups")
    public ResponseEntity<String> createGroup(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups", HttpMethod.POST, body, req);
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<String> getMessages(@PathVariable String groupId, HttpServletRequest req) {
        return proxy.forward(discussionApiBase() + "/groups/" + groupId + "/messages", HttpMethod.GET, null, req);
    }
}
