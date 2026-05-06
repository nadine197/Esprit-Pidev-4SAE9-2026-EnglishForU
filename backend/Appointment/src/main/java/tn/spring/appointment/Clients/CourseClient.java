package tn.spring.appointment.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Remplace 8082 par le port REEL de ton microservice Course !
@FeignClient(name = "COURSE-SERVICE", url = "http://localhost:8082/api/study-groups")
public interface CourseClient {

    // Cette méthode va appeler le GET du microservice Course pour récupérer le groupe par son ID
    @GetMapping("/{id}")
    StudyGroupDTO getStudyGroupById(@PathVariable("id") String id);
}