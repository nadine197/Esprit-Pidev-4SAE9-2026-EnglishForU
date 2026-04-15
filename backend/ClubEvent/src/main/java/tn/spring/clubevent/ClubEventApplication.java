package tn.spring.clubevent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClubEventApplication {
	public static void main(String[] args) {
		SpringApplication.run(ClubEventApplication.class, args);
	}
}
