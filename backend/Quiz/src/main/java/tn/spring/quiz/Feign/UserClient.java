package tn.spring.quiz.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import tn.spring.quiz.Config.ForwardAuthFeignConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "User",
        configuration = ForwardAuthFeignConfiguration.class
)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID id);

    @GetMapping("/api/users/students")
    List<UserDTO> getAllStudents();

    @GetMapping("/api/users/tutors")
    List<UserDTO> getAllTutors();

    @GetMapping("/api/users/admins")
    List<UserDTO> getAllAdmins();
}