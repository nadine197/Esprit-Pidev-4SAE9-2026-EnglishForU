package tn.spring.quiz.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.quiz.Feign.CourseClient;
import tn.spring.quiz.Feign.CourseDTO;
import tn.spring.quiz.Feign.UserClient;
import tn.spring.quiz.Feign.UserDTO;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Repositories.QuizRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private CourseClient courseClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz;
    private Long quizId = 1L;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Java Basics");
        
        Course course = new Course();
        course.setCourseid(101L);
        quiz.setCourse(course);
    }

    @Test
    void getQuizById_ShouldReturnQuiz() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        Quiz found = quizService.getQuizById(quizId);

        assertNotNull(found);
        assertEquals("Java Basics", found.getTitle());
    }

    @Test
    void getCourseInfoFromService_ShouldReturnCourseDTO() {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCourseid(101);
        courseDTO.setTitle("Java Course");

        when(courseClient.getCourseById(101)).thenReturn(courseDTO);

        CourseDTO result = quizService.getCourseInfoFromService(101L);

        assertNotNull(result);
        assertEquals("Java Course", result.getTitle());
        verify(courseClient, times(1)).getCourseById(101);
    }

    @Test
    void getUserInfoFromService_ShouldReturnUserDTO() {
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setEmail("test@test.com");

        when(userClient.getUserById(userId)).thenReturn(userDTO);

        UserDTO result = quizService.getUserInfoFromService(userId);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }
}
