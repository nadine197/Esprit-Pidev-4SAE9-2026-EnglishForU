package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizProxyController {

    private final ProxyForwarder proxy;
    private final String quizServiceBase;

    public QuizProxyController(
            ProxyForwarder proxy,
            @Value("${services.quiz.url}") String quizServiceUrl
    ) {
        this.proxy = proxy;
        this.quizServiceBase = normalizeBaseUrl(quizServiceUrl) + "/api/quizzes";
    }

    @GetMapping("/all")
    public ResponseEntity<String> getAllQuizzes(HttpServletRequest req) {
        return proxy.forward(quizServiceBase, HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getQuizById(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(quizServiceBase + "/" + id, HttpMethod.GET, null, req);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<String> getQuizzesByCourse(@PathVariable Long courseId, HttpServletRequest req) {
        return proxy.forward(quizServiceBase + "/course/" + courseId, HttpMethod.GET, null, req);
    }

    private String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
