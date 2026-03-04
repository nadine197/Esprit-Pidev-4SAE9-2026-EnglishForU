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
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    @PostMapping
    public void create(@Valid @RequestBody CreatePromoCodeRequest req) {
        promoService.createPromo(req);
    }

    // ADMIN
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    @GetMapping
    public List<PromoCode> getAll() {
        return promoService.getAll();
    }



    // ADMIN
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    @PutMapping("/{id}")
    public PromoCode update(@PathVariable Long id, @Valid @RequestBody PromoCode req) {
        return promoService.update(id, req);
    }

    // ADMIN
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        promoService.delete(id);
    }
    // ENABLE PROMO
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable Long id) {
        promoService.enable(id);
    }

    // DISABLE PROMO
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable Long id) {
        promoService.disable(id);
    }
    // STUDENT or ADMIN
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','STUDENT')")
    @PostMapping("/validate")
    public ApplyPromoResponse validate(@Valid @RequestBody ApplyPromoRequest req) {
        return promoService.validateAndCompute(req);
    }
}