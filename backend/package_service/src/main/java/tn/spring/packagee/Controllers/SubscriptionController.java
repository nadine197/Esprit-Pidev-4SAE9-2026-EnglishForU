package tn.spring.packagee.Controllers;


import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.CreateSubscriptionRequest;
import tn.spring.packagee.DTOs.SubscriptionResponse;
import tn.spring.packagee.Services.SubscriptionService;


import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    public SubscriptionResponse create(@Valid @RequestBody CreateSubscriptionRequest req) {
        return service.createSubscription(req);
    }
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','STUDENT')")

    @GetMapping("/student/{studentId}")
    public List<SubscriptionResponse> byStudent(@PathVariable Long studentId) {
        return service.listByStudent(studentId);
    }
}