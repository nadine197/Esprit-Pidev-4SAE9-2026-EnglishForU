package tn.spring.clubevent.Communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", url = "http://localhost:8081/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    Map<String, Object> getUserById(@PathVariable("id") String id);
}
