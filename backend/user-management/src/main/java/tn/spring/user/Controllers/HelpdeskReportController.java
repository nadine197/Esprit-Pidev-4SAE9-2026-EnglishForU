package tn.spring.user.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.spring.user.DTOs.ReportResponse;
import tn.spring.user.DTOs.UpdateReportRequest;
import tn.spring.user.Enums.ReportStatus;
import tn.spring.user.Services.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('HELP_DESK')")
public class HelpdeskReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<List<ReportResponse>> listReports(@RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.getHelpDeskReports(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReportResponse> updateReport(@PathVariable Long id,
                                                       @RequestBody UpdateReportRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(reportService.updateReport(id, request, authentication.getName()));
    }
}
