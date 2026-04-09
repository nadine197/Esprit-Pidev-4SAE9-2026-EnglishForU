package tn.spring.quiz.Config;

import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.spring.quiz.Models.*;
import tn.spring.quiz.Repositories.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;

    public DataInitializer(CourseRepository courseRepository,
                           QuizRepository quizRepository) {
        this.courseRepository = courseRepository;
        this.quizRepository = quizRepository;
    }

    @Override
    public void run(String... args) {

        Faker faker = new Faker();

        if (courseRepository.count() > 0) {
            System.out.println("Courses already exist, skipping initialization.");
            return;
        }

        // =========================
        // 1️⃣ CREATE COURSES
        // =========================
        List<Course> courses = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Course course = new Course();
            course.setTitle(faker.educator().course());
            course.setDescription(faker.lorem().sentence());
            course.setDuration(faker.number().numberBetween(5, 30));
            course.setCreator(faker.number().randomNumber());

            course.setContents(new ArrayList<>());
            course.setQuizzes(new ArrayList<>());

            courses.add(course);
        }

        courseRepository.saveAll(courses);
        System.out.println("10 Courses added ");

        // =========================
        // 2️⃣ GENERATE DATA
        // =========================
        for (Course course : courses) {

            // ---------- CONTENT ----------
            int contentCount = faker.number().numberBetween(3, 6);
            List<Content> contents = new ArrayList<>();

            for (int j = 0; j < contentCount; j++) {
                Content content = new Content();
                content.setTitle(faker.book().title());
                content.setType("VIDEO");
                content.setUrl("https://video.example.com/" + faker.internet().uuid());
                content.setCourse(course);
                contents.add(content);
            }

            course.setContents(contents);

            // ---------- QUIZZES ----------
            int quizCount = faker.number().numberBetween(1, 3);
            List<Quiz> quizzes = new ArrayList<>();

            for (int q = 0; q < quizCount; q++) {

                Quiz quiz = new Quiz();
                quiz.setTitle("Quiz: " + faker.lorem().sentence(3));
                quiz.setCourse(course);
                quiz.setQuestions(new ArrayList<>());

                // ---------- QUESTIONS ----------
                int questionCount = faker.number().numberBetween(3, 5);

                for (int qc = 0; qc < questionCount; qc++) {

                    Question question = new Question();
                    question.setText(faker.lorem().sentence());
                    question.setQuiz(quiz);
                    question.setAnswers(new ArrayList<>());

                    // ---------- ANSWERS ----------
                    String[] options = {"A", "B", "C", "D"};
                    int correctIndex = faker.number().numberBetween(0, options.length);

                    for (int i = 0; i < options.length; i++) {
                        Answer answer = new Answer();
                        answer.setText(options[i]);
                        answer.setCorrect(i == correctIndex);
                        answer.setQuestion(question);
                        question.getAnswers().add(answer);
                    }

                    quiz.getQuestions().add(question);
                }

                quizzes.add(quiz);
            }

            course.setQuizzes(quizzes);

            // ✅ UNE SEULE SAVE (cascade fait tout)
            courseRepository.save(course);
        }

        System.out.println("Fake contents, quizzes, questions and answers generated");
    }
}