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
    private final String appointmentServiceBaseUrl;

    public AppointmentProxyController(ProxyForwarder proxy,
                                      @Value("${services.appointment.base-url:http://localhost:8087}") String appointmentServiceBaseUrl) {
        this.proxy = proxy;
        this.appointmentServiceBaseUrl = appointmentServiceBaseUrl;
    }

    @PostMapping("/book")
    public ResponseEntity<String> book(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/book", HttpMethod.POST, body, req);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<String> getSlots(HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/available-slots", HttpMethod.GET, null, req);
    }

    @PostMapping("/slots")
    public ResponseEntity<String> addSlot(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/slots", HttpMethod.POST, body, req);
    }

    @GetMapping("/all")
    public ResponseEntity<String> getAll(@RequestParam Map<String, String> params, HttpServletRequest req) {
        // This handles pagination and search parameters
        String queryString = req.getQueryString() != null ? "?" + req.getQueryString() : "";
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/all" + queryString, HttpMethod.GET, null, req);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<String> accept(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/" + id + "/accept", HttpMethod.PUT, null, req);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/" + id + "/cancel", HttpMethod.PUT, null, req);
    }



    @PutMapping("/{id}/reschedule")
    public ResponseEntity<String> reschedule(@PathVariable String id, @RequestParam String newDate, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/" + id + "/reschedule?newDate=" + newDate, HttpMethod.PUT, null, req);
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<String> validate(@PathVariable String code, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/validate/" + code, HttpMethod.GET, null, req);
    }

    // À ajouter dans AppointmentProxyController.java

    @PostMapping("/verify-access")
    public ResponseEntity<String> verifyAccess(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/verify-access", HttpMethod.POST, body, req);
    }
    // Dans AppointmentProxyController.java (Gateway)
    @PutMapping("/{id}/complete")
    public ResponseEntity<String> complete(
            @PathVariable String id,
            @RequestParam String result,
            @RequestParam String score,
            @RequestParam int cheatCount,
            HttpServletRequest req) {

        // On construit l'URL manuellement pour être SÛR que tout est envoyé au port 8087
        String url = appointmentServiceBaseUrl + "/api/appointments/" + id + "/complete" +
                "?result=" + result +
                "&score=" + score +
                "&cheatCount=" + cheatCount;

        // On nettoie les espaces éventuels dans l'URL
        url = url.replace(" ", "%20");

        return proxy.forward(url, HttpMethod.PUT, null, req);
    }

    // Dans ta GATEWAY (Port 8090)
    @PostMapping("/groups")
    public ResponseEntity<String> createGroup(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/discussions/groups", HttpMethod.POST, body, req);
    }

    // Dans AppointmentProxyController.java (Gateway 8090)

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<String> getMessages(@PathVariable String groupId, HttpServletRequest req) {
        return proxy.forward(appointmentServiceBaseUrl + "/api/appointments/groups/" + groupId + "/messages", HttpMethod.GET, null, req);
    }
}