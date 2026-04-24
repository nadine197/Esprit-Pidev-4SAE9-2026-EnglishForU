package tn.spring.quiz.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Utilisation de la source de configuration CORS définie plus bas
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ✅ Autoriser OPTIONS (pré-flight) pour le protocole CORS
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Endpoints Publics
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/evaluations/motivation-suggestions").permitAll()

                        // ✅ Recommandations
                        .requestMatchers("/api/quizzes/*/recommendations").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")

                        // ✅ Opérations d'écriture (POST, PUT, DELETE)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/quizzes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/quizzes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/quizzes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")

                        // ✅ Accès public en lecture seule (GET)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/quizzes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/courses/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/quiz-attempts/certificates/**").permitAll()

                        // ✅ Sécurité globale
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ LA CORRECTION : Utiliser setAllowedOriginPatterns au lieu de setAllowedOrigins
        // Cela permet d'utiliser "*" tout en ayant allowCredentials à true.
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200", "*"));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers autorisés (Authorization est crucial pour le JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));

        // Autoriser l'envoi de credentials (Token, Cookies)
        configuration.setAllowCredentials(true);

        // Temps de mise en cache de la réponse CORS (3600 secondes = 1 heure)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}