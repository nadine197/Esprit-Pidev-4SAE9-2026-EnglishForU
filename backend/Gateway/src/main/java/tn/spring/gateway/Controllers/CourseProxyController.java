package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CourseProxyController {

    private final ProxyForwarder proxy;

    public CourseProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    private static final String COURSE_BASE    = "http://localhost:8084/api/courses";
    private static final String CONTENT_BASE   = "http://localhost:8084/api/contents";
    private static final String GROUP_BASE     = "http://localhost:8084/api/study-groups";

    // ══════════════════════════════════════════════════════════
    //  COURSES
    // ══════════════════════════════════════════════════════════

    @GetMapping("/api/courses")
    public ResponseEntity<String> getAllCourses(HttpServletRequest req) {
        return proxy.forward(COURSE_BASE, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/courses/{id}")
    public ResponseEntity<String> getCourseById(@PathVariable int id, HttpServletRequest req) {
        return proxy.forward(COURSE_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    @PostMapping("/api/courses")
    public ResponseEntity<String> createCourse(@RequestBody Map<String, Object> body,
                                               HttpServletRequest req) {
        return proxy.forward(COURSE_BASE, HttpMethod.POST, body, req);
    }

    @PutMapping("/api/courses/{id}")
    public ResponseEntity<String> updateCourse(@PathVariable int id,
                                               @RequestBody Map<String, Object> body,
                                               HttpServletRequest req) {
        return proxy.forward(COURSE_BASE + "/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/api/courses/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable int id, HttpServletRequest req) {
        return proxy.forward(COURSE_BASE + "/" + id, HttpMethod.DELETE, null, req);
    }

    @GetMapping("/api/courses/sort/duration")
    public ResponseEntity<String> sortByDuration(HttpServletRequest req) {
        return proxy.forward(COURSE_BASE + "/sort/duration", HttpMethod.GET, null, req);
    }

    @GetMapping("/api/courses/search")
    public ResponseEntity<String> searchCourses(HttpServletRequest req) {
        String keyword = req.getParameter("keyword");
        return proxy.forward(COURSE_BASE + "/search?keyword=" + keyword, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/courses/pdf")
    public ResponseEntity<String> exportCoursesPdf(HttpServletRequest req) {
        return proxy.forward(COURSE_BASE + "/pdf", HttpMethod.GET, null, req);
    }

    @GetMapping("/api/courses/excel")
    public ResponseEntity<String> exportCoursesExcel(HttpServletRequest req) {
        return proxy.forward(COURSE_BASE + "/excel", HttpMethod.GET, null, req);
    }

    // ══════════════════════════════════════════════════════════
    //  CONTENTS
    // ══════════════════════════════════════════════════════════

    @GetMapping("/api/contents")
    public ResponseEntity<String> getAllContents(HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/contents/{id}")
    public ResponseEntity<String> getContentById(@PathVariable int id, HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    @PostMapping("/api/contents")
    public ResponseEntity<String> createContent(@RequestBody Map<String, Object> body,
                                                HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE, HttpMethod.POST, body, req);
    }

    @PutMapping("/api/contents/{id}")
    public ResponseEntity<String> updateContent(@PathVariable int id,
                                                @RequestBody Map<String, Object> body,
                                                HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE + "/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/api/contents/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable int id, HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE + "/" + id, HttpMethod.DELETE, null, req);
    }

    @GetMapping("/api/contents/search")
    public ResponseEntity<String> searchContents(HttpServletRequest req) {
        String keyword = req.getParameter("keyword");
        return proxy.forward(CONTENT_BASE + "/search?keyword=" + keyword, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/contents/pdf")
    public ResponseEntity<String> exportContentsPdf(HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE + "/pdf", HttpMethod.GET, null, req);
    }

    @GetMapping("/api/contents/stats/type")
    public ResponseEntity<String> contentStatsByType(HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE + "/stats/type", HttpMethod.GET, null, req);
    }

    @GetMapping("/api/contents/history/txt")
    public ResponseEntity<String> downloadContentHistory(HttpServletRequest req) {
        return proxy.forward(CONTENT_BASE + "/history/txt", HttpMethod.GET, null, req);
    }

    // ══════════════════════════════════════════════════════════
    //  STUDY GROUPS
    // ══════════════════════════════════════════════════════════

    @GetMapping("/api/study-groups")
    public ResponseEntity<String> getAllGroups(HttpServletRequest req) {
        return proxy.forward(GROUP_BASE, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/study-groups/{id}")
    public ResponseEntity<String> getGroupById(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/" + id, HttpMethod.GET, null, req);
    }

    @PostMapping("/api/study-groups")
    public ResponseEntity<String> createGroup(@RequestBody Map<String, Object> body,
                                              HttpServletRequest req) {
        return proxy.forward(GROUP_BASE, HttpMethod.POST, body, req);
    }

    @PutMapping("/api/study-groups/{id}")
    public ResponseEntity<String> updateGroup(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body,
                                              HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/api/study-groups/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/" + id, HttpMethod.DELETE, null, req);
    }

    @PostMapping("/api/study-groups/validated")
    public ResponseEntity<String> createGroupValidated(@RequestBody Map<String, Object> body,
                                                       HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/validated", HttpMethod.POST, body, req);
    }

    @GetMapping("/api/study-groups/search")
    public ResponseEntity<String> searchGroups(HttpServletRequest req) {
        String query = req.getQueryString();
        String url = query != null ? GROUP_BASE + "/search?" + query : GROUP_BASE + "/search";
        return proxy.forward(url, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/study-groups/stats")
    public ResponseEntity<String> getGroupStats(HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/stats", HttpMethod.GET, null, req);
    }

    @GetMapping("/api/study-groups/{id}/audit")
    public ResponseEntity<String> getAuditLog(@PathVariable Long id, HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/" + id + "/audit", HttpMethod.GET, null, req);
    }

    @GetMapping("/api/study-groups/calendar/by-date")
    public ResponseEntity<String> groupsByDate(HttpServletRequest req) {
        String date = req.getParameter("date");
        return proxy.forward(GROUP_BASE + "/calendar/by-date?date=" + date, HttpMethod.GET, null, req);
    }

    @GetMapping("/api/study-groups/calendar/by-month")
    public ResponseEntity<String> groupsByMonth(HttpServletRequest req) {
        String year  = req.getParameter("year");
        String month = req.getParameter("month");
        return proxy.forward(GROUP_BASE + "/calendar/by-month?year=" + year + "&month=" + month,
                HttpMethod.GET, null, req);
    }

    @GetMapping("/api/study-groups/calendar/marked-dates")
    public ResponseEntity<String> markedDates(HttpServletRequest req) {
        String year  = req.getParameter("year");
        String month = req.getParameter("month");
        return proxy.forward(GROUP_BASE + "/calendar/marked-dates?year=" + year + "&month=" + month,
                HttpMethod.GET, null, req);
    }

    @PostMapping("/api/study-groups/chatbot")
    public ResponseEntity<String> chatbot(HttpServletRequest req) {
        String query = req.getQueryString();
        String url = query != null ? GROUP_BASE + "/chatbot?" + query : GROUP_BASE + "/chatbot";
        return proxy.forward(url, HttpMethod.POST, null, req);
    }

    @PostMapping("/api/study-groups/scheduler/run")
    public ResponseEntity<String> runScheduler(HttpServletRequest req) {
        return proxy.forward(GROUP_BASE + "/scheduler/run", HttpMethod.POST, null, req);
    }
}
