package tn.spring.user.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.user.Models.User;
import tn.spring.user.Services.UserService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/students")
    public ResponseEntity<?> getAllstudents() {
        try {
            return ResponseEntity.ok(userService.getAllStudents());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/evaluations")
    public ResponseEntity<?> getEvaluations(@RequestParam String studentId) {
        // Retourne une liste vide pour éviter l'erreur "length" sur Angular
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/users/ml/statistics")
    public ResponseEntity<?> getMlStatistics(@RequestParam String dataset) {
        // Retourne un objet structuré pour éviter les erreurs de lecture
        HashMap<String, Object> response = new HashMap<>();
        response.put("files", new ArrayList<>());
        return ResponseEntity.ok(response);
    }
}