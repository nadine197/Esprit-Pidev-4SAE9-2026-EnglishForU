package tn.spring.packagee.Controllers;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.AddPackageItemRequest;
import tn.spring.packagee.DTOs.CreatePackageOfferRequest;
import tn.spring.packagee.DTOs.PackageOfferResponse;
import tn.spring.packagee.Entities.PackageOffer;
import tn.spring.packagee.Services.PackageOfferService;


import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageOfferController {

    private final PackageOfferService service;

    public PackageOfferController(PackageOfferService service) {
        this.service = service;
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public PackageOfferResponse create(@Valid @RequestBody CreatePackageOfferRequest req) {
        return service.create(req);
    }
    // ✅ ADMIN: get all (active + inactive)
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PackageOfferResponse> all() {
        return service.listAll();
    }
    @GetMapping("/{id}")
    public PackageOfferResponse byId(@PathVariable Long id) {
        return service.getByID(id);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    // ✅ ADMIN: update package
    @PutMapping("/{id}")
    public PackageOfferResponse update(@PathVariable Long id,
                                       @Valid @RequestBody PackageOffer req) {
        return service.update(id, req);
    }

    // ✅ ADMIN: disable (soft delete)
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable Long id) {
        service.setActive(id, false);
    }

    // ✅ ADMIN: enable
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable Long id) {
        service.setActive(id, true);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/items")
    public PackageOfferResponse addItem(@PathVariable Long id, @Valid @RequestBody AddPackageItemRequest req) {
        return service.addItem(id, req);
    }
    @GetMapping("/active")
    public List<PackageOfferResponse> active() {
        return service.listActive();
    }
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT')")
    @GetMapping("/search")
    public List<PackageOfferResponse> search(@RequestParam String q) {
        return service.searchByName(q);
    }
}