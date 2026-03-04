package tn.spring.quiz.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Repositories.AnswerRepository;
import tn.spring.quiz.Repositories.QuestionRepository;
import tn.spring.quiz.Repositories.QuizRepository;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final AnswerRepository answerRepository;
    public QuestionService(QuestionRepository questionRepository,
                           QuizRepository quizRepository,
                           AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.answerRepository = answerRepository;
    }


    @Transactional
    public Question createQuestion(Long quizId, Question question) {
        // Vérifier que le quiz existe
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Associer la question au quiz
        question.setQuiz(quiz);

        // Associer chaque réponse à la question
        List<Answer> answers = question.getAnswers();
        if (answers != null) {
            for (Answer answer : answers) {
                answer.setQuestion(question);
            }
        }

        // Ajouter la question à la liste du quiz
        List<Question> quizQuestions = quiz.getQuestions();
        quizQuestions.add(question);
        quiz.setQuestions(quizQuestions);

        // Sauvegarder la question (cascade devrait persister les réponses)
        return questionRepository.save(question);
    }
    @Transactional
    public Question updateQuestion(Long questionId, Question updatedQuestion) {
        // Récupérer la question existante
        Question existingQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Mettre à jour le texte de la question
        existingQuestion.setText(updatedQuestion.getText());

        // Sauvegarder la question (cascade persiste les réponses)
        return questionRepository.save(existingQuestion);
    }
    // ➕ Ajouter une question à un quiz
    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        question.setQuiz(quiz);

        // Associer les réponses
        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                answer.setQuestion(question);
            }
        }

        quiz.getQuestions().add(question);
        return questionRepository.save(question);
    }
    // ✏️ Modifier une réponse
    @Transactional
    public Answer updateAnswer(Long answerId, Answer answerDetails) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        answer.setText(answerDetails.getText());
        answer.setCorrect(answerDetails.isCorrect());
        return answerRepository.save(answer);
    }

    // 🗑 Supprimer une réponse
    @Transactional
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        answerRepository.delete(answer);
    }
    // 🗑 Supprimer une question
    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Supprimer les réponses si cascade non défini
        answerRepository.deleteAllByQuestionId(questionId);

        questionRepository.delete(question);
    }

    // ➕ Ajouter une réponse à une question existante
    @Transactional
    public Answer addAnswer(Long questionId, Answer answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        answer.setQuestion(question);
        question.getAnswers().add(answer);
        return answerRepository.save(answer);
    }
}