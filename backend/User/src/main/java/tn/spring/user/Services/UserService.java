package tn.spring.user.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.user.Enums.AvailableStatus;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.Student;
import tn.spring.user.Models.Tutor;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.StudentRepos;
import tn.spring.user.Repositories.TutorRepos;
import tn.spring.user.Repositories.UserRepos;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private static final String DEFAULT_PHONE_PREFIX = "+216";

    private final UserRepos userRepos;

    private final StudentRepos studentRepos;

    private final TutorRepos tutorRepos;
    private final PasswordEncoder passwordEncoder;

    // SUPER_ADMIN → create any user
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        return userRepos.save(user);
    }
    @Transactional
    public void changeRole(UUID id, UserRole newRole, UserRole actorRole) {

        if (actorRole != UserRole.SUPER_ADMIN)
            throw new SecurityException("Only SUPER_ADMIN can change roles");

        User user = getById(id);
        user.setRole(newRole);
        userRepos.save(user);
    }
    // Dans UserService.java

    public User createStudentOrTutor(User user) {
        // 1. Sécurité : Empêcher de créer un admin via cet endpoint
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)
            throw new RuntimeException("Access denied: Use create-employee for admins");

        // 2. Encodage du mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        String normalizedPrefix = (user.getPrefix() == null || user.getPrefix().isBlank())
                ? DEFAULT_PHONE_PREFIX
                : user.getPrefix().trim();
        user.setPrefix(normalizedPrefix);

        // 3. Logique d'héritage pour le mode JOINED
        if (user.getRole() == UserRole.STUDENT) {
            // On crée un vrai objet Student pour que Hibernate remplisse la table 'student'
            Student student = Student.builder()
                    .name(user.getName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .phone(user.getPhone())
                    .prefix(normalizedPrefix)
                    .role(UserRole.STUDENT)
                    .active(true)
                    .englishLevel("A1") // Valeurs par défaut requises par votre DB
                    .learningGoal("General")
                    .dailyGoalMinutes(0)
                    .build();
            return studentRepos.save(student);
        }

        if (user.getRole() == UserRole.TUTOR) {
            Tutor tutor = Tutor.builder()
                    .name(user.getName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .phone(user.getPhone())
                    .prefix(normalizedPrefix)
                    .role(UserRole.TUTOR)
                    .active(true)
                    .verified(false)
                    .availability_status(AvailableStatus.AVAILABLE)
                    .experience_years(0)
                    .rating(0)
                    .build();
            return tutorRepos.save(tutor);
        }

        return userRepos.save(user);
    }

    public User updateUser(UUID id, User updated) {
        User u = getById(id);

        u.setName(updated.getName());
        u.setLastName(updated.getLastName());
        u.setPhone(updated.getPhone());
        u.setPrefix(updated.getPrefix());
        u.setAddress(updated.getAddress());

        return userRepos.save(u);
    }

    public void blockUser(UUID id) {
        User u = getById(id);
        u.setActive(false);
        userRepos.save(u);
    }

    public void unblockUser(UUID id) {
        User u = getById(id);
        u.setActive(true);
        userRepos.save(u);
    }

    public List<User> getAllAdmins() {
        return userRepos.findByRoleIn(
                List.of(UserRole.SUPER_ADMIN, UserRole.ADMIN)
        );
    }
    public List<Student> getAllStudents() {
        return studentRepos.findAll();
    }
    public List<Tutor> getAllTutors() {
        return tutorRepos.findAll();
    }

    public User getById(UUID id) {
        return userRepos.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
