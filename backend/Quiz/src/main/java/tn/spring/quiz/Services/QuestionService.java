package tn.spring.quiz.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Repositories.AnswerRepository;
import tn.spring.quiz.Repositories.QuestionRepository;
import tn.spring.quiz.Repositories.QuizRepository;

import java.util.ArrayList;
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
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        question.setId(null);
        question.setQuiz(quiz);

        List<Answer> answers = question.getAnswers();
        if (answers == null) {
            question.setAnswers(new ArrayList<>());
        } else {
            for (Answer answer : answers) {
                answer.setId(null);
                answer.setQuestion(question);
            }
        }

        quiz.getQuestions().add(question);
        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(Long questionId, Question updatedQuestion) {
        Question existingQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        existingQuestion.setText(updatedQuestion.getText());
        return questionRepository.save(existingQuestion);
    }

    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        question.setId(null);
        question.setQuiz(quiz);

        if (question.getAnswers() == null) {
            question.setAnswers(new ArrayList<>());
        } else {
            for (Answer answer : question.getAnswers()) {
                answer.setId(null);
                answer.setQuestion(question);
            }
        }

        quiz.getQuestions().add(question);
        return questionRepository.save(question);
    }

    @Transactional
    public Answer updateAnswer(Long answerId, Answer answerDetails) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        answer.setText(answerDetails.getText());
        answer.setCorrect(answerDetails.isCorrect());
        return answerRepository.save(answer);
    }

    @Transactional
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        answerRepository.delete(answer);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        answerRepository.deleteAllByQuestionId(questionId);
        questionRepository.delete(question);
    }

    @Transactional
    public Answer addAnswer(Long questionId, Answer answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        answer.setId(null);
        answer.setQuestion(question);

        if (question.getAnswers() == null) {
            question.setAnswers(new ArrayList<>());
        }

        question.getAnswers().add(answer);
        return answerRepository.save(answer);
    }
}
