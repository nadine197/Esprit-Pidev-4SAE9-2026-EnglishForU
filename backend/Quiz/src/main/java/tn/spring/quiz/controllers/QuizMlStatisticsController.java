package tn.spring.quiz.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.quiz.Services.MlStatisticsFileService;

import java.util.Map;

/**
 * ML chart listing is served under {@code /api/quiz/ml/...} so paths are not mistaken for
 * {@code /api/evaluations/{id}} in Spring MVC.
 */
@RestController
@RequestMapping("/api/quiz/ml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizMlStatisticsController {

    private final MlStatisticsFileService mlStatisticsFileService;

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<Map<String, Object>> listMlStatistics(
            @RequestParam(defaultValue = "facial") String dataset
    ) {
        return ResponseEntity.ok(mlStatisticsFileService.listPngDescriptors(dataset));
    }

    @GetMapping(value = "/statistics/file", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<byte[]> getMlStatisticsFile(
            @RequestParam(defaultValue = "facial") String dataset,
            @RequestParam String file
    ) {
        return mlStatisticsFileService.readPngFile(dataset, file)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(bytes))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chart not found"));
    }
}
