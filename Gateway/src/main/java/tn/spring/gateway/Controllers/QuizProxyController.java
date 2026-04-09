package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizProxyController {

    private final ProxyForwarder proxy;

    public QuizProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    private static final String QUIZ_SERVICE_BASE = "http://localhost:8056/api/quizzes"; // microservice Quiz

    // GET all quizzes
    @GetMapping("/all")
    public ResponseEntity<String> getAllQuizzes(HttpServletRequest req) {
        // 🔹 juste forward vers /api/quizzes
        return proxy.forward(QUIZ_SERVICE_BASE, HttpMethod.GET, null, req);
    }

    // GET quiz by id
    @GetMapping("/{id}")
    public ResponseEntity<String> getQuizById(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(QUIZ_SERVICE_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    // GET quizzes by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<String> getQuizzesByCourse(@PathVariable Long courseId, HttpServletRequest req) {
        return proxy.forward(QUIZ_SERVICE_BASE + "/course/" + courseId, HttpMethod.GET, null, req);
    }
}