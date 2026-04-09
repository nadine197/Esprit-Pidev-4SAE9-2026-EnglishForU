package tn.spring.user.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.spring.user.DTOs.CreateReportCommentRequest;
import tn.spring.user.DTOs.CreateReportRequest;
import tn.spring.user.DTOs.ReportActivityResponse;
import tn.spring.user.DTOs.ReportCommentResponse;
import tn.spring.user.DTOs.ReportResponse;
import tn.spring.user.Services.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request,
                                                       Authentication authentication) {
        ReportResponse response = reportService.createReport(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReportResponse>> getMyReports(Authentication authentication) {
        return ResponseEntity.ok(reportService.getMyReports(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id,
                                                        Authentication authentication) {
        return ResponseEntity.ok(reportService.getReportDetails(id, authentication.getName()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ReportCommentResponse>> listComments(@PathVariable Long id,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(reportService.listReportComments(id, authentication.getName()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ReportCommentResponse> addComment(@PathVariable Long id,
                                                            @Valid @RequestBody CreateReportCommentRequest request,
                                                            Authentication authentication) {
        ReportCommentResponse response = reportService.addReportComment(id, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<List<ReportActivityResponse>> listActivity(@PathVariable Long id,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(reportService.listReportActivity(id, authentication.getName()));
    }
}
