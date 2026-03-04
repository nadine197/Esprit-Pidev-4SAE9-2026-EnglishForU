package tn.spring.quiz.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.DTO.QuizDTO;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Repositories.AnswerRepository;
import tn.spring.quiz.Repositories.CourseRepository;
import tn.spring.quiz.Repositories.QuestionRepository;
import tn.spring.quiz.Repositories.QuizRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    // Create
    public Quiz createQuiz(Long courseId, Quiz quiz) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        quiz.setCourse(course);
        quiz.setPassingScore(70); // fixe le passingScore
        quiz.setQuestions(new ArrayList<>()); // initialise liste vide

        return quizRepository.save(quiz);
    }

    // Update
    @Transactional
    public Quiz updateQuiz(Long id, Quiz quizDetails) {
        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Mise à jour du titre
        if (quizDetails.getTitle() != null) {
            existingQuiz.setTitle(quizDetails.getTitle());
        }

        // Préparer les questions entrantes
        List<Question> incomingQuestions = quizDetails.getQuestions() != null
                ? quizDetails.getQuestions()
                : new ArrayList<>();

        // S'assurer que la liste des questions existante est initialisée
        List<Question> existingQuestions = existingQuiz.getQuestions() != null
                ? existingQuiz.getQuestions()
                : new ArrayList<>();

        // Supprimer les questions qui n'existent plus
        existingQuestions.removeIf(existingQuestion ->
                incomingQuestions.stream()
                        .noneMatch(q -> q.getId() != null && q.getId().equals(existingQuestion.getId()))
        );

        // Traiter chaque question entrante
        for (Question incomingQuestion : incomingQuestions) {
            if (incomingQuestion.getId() == null) {
                // Nouvelle question
                incomingQuestion.setQuiz(existingQuiz);

                // Associer les réponses
                if (incomingQuestion.getAnswers() != null) {
                    for (Answer answer : incomingQuestion.getAnswers()) {
                        answer.setQuestion(incomingQuestion);
                    }
                } else {
                    incomingQuestion.setAnswers(new ArrayList<>());
                }

                existingQuestions.add(incomingQuestion);
            } else {
                // Question existante
                Question existingQuestion = existingQuestions.stream()
                        .filter(q -> q.getId().equals(incomingQuestion.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Question not found for id " + incomingQuestion.getId()));

                // Mettre à jour le texte de la question
                if (incomingQuestion.getText() != null) {
                    existingQuestion.setText(incomingQuestion.getText());
                }

                List<Answer> incomingAnswers = incomingQuestion.getAnswers() != null
                        ? incomingQuestion.getAnswers()
                        : new ArrayList<>();

                List<Answer> existingAnswers = existingQuestion.getAnswers() != null
                        ? existingQuestion.getAnswers()
                        : new ArrayList<>();

                // Supprimer les réponses supprimées
                existingAnswers.removeIf(existingAnswer ->
                        incomingAnswers.stream()
                                .noneMatch(a -> a.getId() != null && a.getId().equals(existingAnswer.getId()))
                );

                // Ajouter ou mettre à jour les réponses
                for (Answer incomingAnswer : incomingAnswers) {
                    if (incomingAnswer.getId() == null) {
                        // Nouvelle réponse
                        incomingAnswer.setQuestion(existingQuestion);
                        existingAnswers.add(incomingAnswer);
                    } else {
                        // Mettre à jour réponse existante
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

                // S’assurer que la liste des réponses est bien attachée
                existingQuestion.setAnswers(existingAnswers);
            }
        }

        // S’assurer que la liste des questions est bien attachée
        existingQuiz.setQuestions(existingQuestions);

        return quizRepository.save(existingQuiz);
    }

    // Delete
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quizRepository.delete(quiz);
    }

    // Get all
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }
    // Get by id
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    // Get by course
    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findAll()
                .stream()
                .filter(q -> q.getCourse().getCourseid().equals(courseId))
                .toList();
    }
    // Récupérer toutes les questions d'un quiz
    public List<Question> getQuizQuestions(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return questionRepository.findAll()
                .stream()
                .filter(q -> q.getQuiz().getId().equals(quizId))
                .toList();
    }

    // Récupérer les réponses d'une question
    public List<Answer> getAnswerByQuizQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return answerRepository.findAll()
                .stream()
                .filter(a -> a.getQuestion().getId().equals(questionId))
                .toList();
    }
}