package tn.spring.packagee.Controllers;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.*;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Services.PromoService;

import java.util.List;

@RestController
@RequestMapping("/api/promos")
public class PromoController {

    private final PromoService promoService;

    public PromoController(PromoService promoService) {
        this.promoService = promoService;
    }

    // ADMIN
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void create(@Valid @RequestBody CreatePromoCodeRequest req) {
        promoService.createPromo(req);
    }

    // ADMIN
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PromoCode> getAll() {
        return promoService.getAll();
    }



    // ADMIN
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public PromoCode update(@PathVariable Long id, @Valid @RequestBody PromoCode req) {
        return promoService.update(id, req);
    }

    // ADMIN
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        promoService.delete(id);
    }
    // ENABLE PROMO
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void enable(@PathVariable Long id) {
        promoService.enable(id);
    }

    // DISABLE PROMO
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void disable(@PathVariable Long id) {
        promoService.disable(id);
    }
    // STUDENT or ADMIN
    @PostMapping("/validate")
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT')")
    public ApplyPromoResponse validate(@Valid @RequestBody ApplyPromoRequest req) {
        return promoService.validateAndCompute(req);
    }
}