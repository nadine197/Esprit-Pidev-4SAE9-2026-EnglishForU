package tn.spring.packagee.Controllers;


import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.ApplyPromoRequest;
import tn.spring.packagee.DTOs.ApplyPromoResponse;
import tn.spring.packagee.DTOs.CreatePromoCodeRequest;
import tn.spring.packagee.Services.PromoService;


@RestController
@RequestMapping("/api/promos")
public class PromoController {

    private final PromoService promoService;

    public PromoController(PromoService promoService) {
        this.promoService = promoService;
    }
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")

    @PostMapping
    public void create(@Valid @RequestBody CreatePromoCodeRequest req) {
        promoService.createPromo(req);
    }
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','STUDENT')")

    @PostMapping("/validate")
    public ApplyPromoResponse validate(@Valid @RequestBody ApplyPromoRequest req) {
        return promoService.validateAndCompute(req);
    }
}