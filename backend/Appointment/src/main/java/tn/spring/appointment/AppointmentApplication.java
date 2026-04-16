package tn.spring.appointment;

// Correction de l'import ici : on utilise le package "models" et non "annotations"
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableFeignClients
@SpringBootApplication
public class AppointmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentApplication.class, args);
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info() // Maintenant, "new Info()" est valide
						.title("EnglishForU - Appointment API")
						.version("1.0")
						.description("Documentation des services de réservation et de discussion en temps réel."));
	}
}