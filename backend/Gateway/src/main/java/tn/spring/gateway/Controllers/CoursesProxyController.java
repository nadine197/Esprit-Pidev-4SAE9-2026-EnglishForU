package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CoursesProxyController {

    private final ProxyForwarder proxy;

    public CoursesProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    private static final String COURSES_SERVICE_BASE = "http://localhost:8056/api/courses"; // base correcte du microservice Course

    // ======= COURSES =======
    @GetMapping("/all")
    public ResponseEntity<String> getAllCourses(HttpServletRequest req) {
        // plus de "/courses/all" en double
        return proxy.forward(COURSES_SERVICE_BASE + "/all", HttpMethod.GET, null, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getCourseById(@PathVariable Long id, HttpServletRequest req) {
        // plus de "/courses/getCourseById/" en double
        return proxy.forward(COURSES_SERVICE_BASE + "/getCourseById/" + id, HttpMethod.GET, null, req);
    }
}