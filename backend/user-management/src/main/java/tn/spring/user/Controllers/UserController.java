package tn.spring.user.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.user.DTOs.ChangeRoleRequest;
import tn.spring.user.Models.Student;
import tn.spring.user.Models.Tutor;
import tn.spring.user.Models.User;
import tn.spring.user.Services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/students")
    // ✅ FIX : Utilisation de hasAnyRole
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Student>> getAllstudents() {
        return ResponseEntity.ok(userService.getAllStudents());
    }

    @GetMapping("/tutors")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Tutor>> getAlltutors() {
        return ResponseEntity.ok(userService.getAllTutors());
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<User> createClient(@RequestBody User user) {
        return ResponseEntity.ok(userService.createStudentOrTutor(user));
    }

    // ... Gardez le reste de vos méthodes en remplaçant hasAnyAuthority par hasAnyRole
}