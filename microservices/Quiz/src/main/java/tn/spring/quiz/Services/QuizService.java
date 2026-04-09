package tn.spring.quiz.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.DTO.QuizDTO;
import tn.spring.quiz.Feign.CourseClient;
import tn.spring.quiz.Feign.CourseDTO;
import tn.spring.quiz.Feign.UserClient;
import tn.spring.quiz.Feign.UserDTO;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Repositories.AnswerRepository;
import tn.spring.quiz.Repositories.CourseRepository;
import tn.spring.quiz.Repositories.QuestionRepository;
import tn.spring.quiz.Repositories.QuizAttemptRepository;
import tn.spring.quiz.Repositories.QuizRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    // ✅ OpenFeign clients
    private final CourseClient courseClient;
    private final UserClient userClient;

    // ================================================
    // MÉTHODES EXISTANTES — inchangées
    // ================================================

    public Quiz createQuiz(Long courseId, Quiz quiz) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        quiz.setCourse(course);
        quiz.setPassingScore(70);
        quiz.setQuestions(new ArrayList<>());
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz updateQuiz(Long id, Quiz quizDetails) {
        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (quizDetails.getTitle() != null) {
            existingQuiz.setTitle(quizDetails.getTitle());
        }

        List<Question> incomingQuestions = quizDetails.getQuestions() != null
                ? quizDetails.getQuestions() : new ArrayList<>();

        List<Question> existingQuestions = existingQuiz.getQuestions() != null
                ? existingQuiz.getQuestions() : new ArrayList<>();

        existingQuestions.removeIf(existingQuestion ->
                incomingQuestions.stream()
                        .noneMatch(q -> q.getId() != null && q.getId().equals(existingQuestion.getId()))
        );

        for (Question incomingQuestion : incomingQuestions) {
            if (incomingQuestion.getId() == null) {
                incomingQuestion.setQuiz(existingQuiz);
                if (incomingQuestion.getAnswers() != null) {
                    for (Answer answer : incomingQuestion.getAnswers()) {
                        answer.setQuestion(incomingQuestion);
                    }
                } else {
                    incomingQuestion.setAnswers(new ArrayList<>());
                }
                existingQuestions.add(incomingQuestion);
            } else {
                Question existingQuestion = existingQuestions.stream()
                        .filter(q -> q.getId().equals(incomingQuestion.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Question not found for id " + incomingQuestion.getId()));

                if (incomingQuestion.getText() != null) {
                    existingQuestion.setText(incomingQuestion.getText());
                }

                List<Answer> incomingAnswers = incomingQuestion.getAnswers() != null
                        ? incomingQuestion.getAnswers() : new ArrayList<>();

                List<Answer> existingAnswers = existingQuestion.getAnswers() != null
                        ? existingQuestion.getAnswers() : new ArrayList<>();

                existingAnswers.removeIf(existingAnswer ->
                        incomingAnswers.stream()
                                .noneMatch(a -> a.getId() != null && a.getId().equals(existingAnswer.getId()))
                );

                for (Answer incomingAnswer : incomingAnswers) {
                    if (incomingAnswer.getId() == null) {
                        incomingAnswer.setQuestion(existingQuestion);
                        existingAnswers.add(incomingAnswer);
                    } else {
                        Answer existingAnswer = existingAnswers.stream()
                                .filter(a -> a.getId().equals(incomingAnswer.getId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Answer not found for id " + incomingAnswer.getId()));

                        if (incomingAnswer.getText() != null) {
                            existingAnswer.setText(incomingAnswer.getText());
                        }
                        existingAnswer.setCorrect(incomingAnswer.isCorrect());
                    }
                }
                existingQuestion.setAnswers(existingAnswers);
            }
        }

        existingQuiz.setQuestions(existingQuestions);
        return quizRepository.save(existingQuiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quizAttemptRepository.deleteAllByQuizId(id);
        quizRepository.delete(quiz);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findAll()
                .stream()
                .filter(q -> q.getCourse().getCourseid().equals(courseId))
                .toList();
    }

    public List<Question> getQuizQuestions(Long quizId) {
        quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return questionRepository.findAllByQuizIdOrderByIdAsc(quizId);
    }

    public List<Answer> getAnswerByQuizQuestion(Long questionId) {
        questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return answerRepository.findAllByQuestionIdOrderByIdAsc(questionId);
    }

    // ================================================
    // ✅ NOUVELLES MÉTHODES OPENFEIGN
    // ================================================

    // Récupère les infos d'un cours depuis Course-service via Feign
    public CourseDTO getCourseInfoFromService(Long courseId) {
        return courseClient.getCourseById(courseId.intValue());
    }


    // Récupère tous les cours depuis Course-service via Feign
    public List<CourseDTO> getAllCoursesFromService() {
        return courseClient.getAllCourses();
    }

    // Récupère les infos d'un utilisateur depuis User-service via Feign
    public UserDTO getUserInfoFromService(UUID userId) {
        return userClient.getUserById(userId);
    }

    // Récupère les infos du cours d'un quiz via Feign
    public CourseDTO getCourseOfQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        Long courseId = quiz.getCourse().getCourseid();
        return courseClient.getCourseById(courseId.intValue());
    }

}