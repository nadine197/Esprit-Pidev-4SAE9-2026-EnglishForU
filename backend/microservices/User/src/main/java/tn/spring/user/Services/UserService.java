package tn.spring.user.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserRepos userRepos;

    private final StudentRepos studentRepos;

    private final TutorRepos tutorRepos;
    private final PasswordEncoder passwordEncoder;

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

    public User createStudentOrTutor(User user) {

        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)
            throw new RuntimeException("Access denied: Use create-employee for admins");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        if (user.getRole() == UserRole.STUDENT) {

            Student student = Student.builder()
                    .name(user.getName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .phone(user.getPhone())
                    .prefix(user.getPrefix() != null ? user.getPrefix() : "+216")
                    .role(UserRole.STUDENT)
                    .active(true)
                    .englishLevel("A1")
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
                    .prefix(user.getPrefix())
                    .role(UserRole.TUTOR)
                    .active(true)
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

    public List<User> getAllUsers() {
        return userRepos.findAll();
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

