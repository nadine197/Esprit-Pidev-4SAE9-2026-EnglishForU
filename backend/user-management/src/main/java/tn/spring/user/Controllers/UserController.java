package tn.spring.user.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.user.DTOs.ChangeRoleRequest;
import tn.spring.user.Enums.UserRole;
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

    // SUPER_ADMIN only
    @PostMapping("/create-employee")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    // ADMIN → student + tutor
    @PostMapping("/create-user")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<User> createClient(@RequestBody User user) {
        return ResponseEntity.ok(userService.createStudentOrTutor(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<User> update(@PathVariable UUID id,
                                       @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PutMapping("/block/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> block(@PathVariable UUID id) {
        userService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/unblock/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> unblock(@PathVariable UUID id) {
        userService.unblockUser(id);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Void> changeRole(@PathVariable UUID id,
                                           @RequestBody ChangeRoleRequest req
                                           ) {

        userService.changeRole(id, req.getNewRole(), UserRole.SUPER_ADMIN);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/admins")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(userService.getAllAdmins());
    }

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<Student>> getAllstudents() {
        return ResponseEntity.ok(userService.getAllStudents());
    }

    @GetMapping("/tutors")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<Tutor>> getAlltutors() {
        return ResponseEntity.ok(userService.getAllTutors());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

}
