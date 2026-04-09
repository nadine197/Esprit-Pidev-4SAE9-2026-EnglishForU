package tn.spring.user.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.StudentRepos;
import tn.spring.user.Repositories.TutorRepos;
import tn.spring.user.Repositories.UserRepos;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepos userRepos;
    @Mock
    private StudentRepos studentRepos;
    @Mock
    private TutorRepos tutorRepos;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("password")
                .role(UserRole.STUDENT)
                .active(true)
                .build();
    }

    @Test
    void getById_ShouldReturnUser() {
        when(userRepos.findById(userId)).thenReturn(Optional.of(user));

        User found = userService.getById(userId);

        assertNotNull(found);
        assertEquals(user.getEmail(), found.getEmail());
    }

    @Test
    void createUser_ShouldEncodePasswordAndSave() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepos.save(any(User.class))).thenReturn(user);

        User created = userService.createUser(user);

        assertNotNull(created);
        verify(passwordEncoder).encode("password");
        verify(userRepos).save(user);
    }

    @Test
    void blockUser_ShouldSetInactive() {
        when(userRepos.findById(userId)).thenReturn(Optional.of(user));

        userService.blockUser(userId);

        assertFalse(user.isActive());
        verify(userRepos).save(user);
    }
}
