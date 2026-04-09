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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // ✅ Autoriser OPTIONS (pré-flight) pour n'importe quelle URL
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ PRIORITÉ 1 : Endpoints de recommandation (doivent être au-dessus car ils sont spécifiques)
                        .requestMatchers("/api/quizzes/*/recommendations").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")

                        // ✅ PRIORITÉ 2 : Opérations d'écriture (POST, PUT, DELETE) pour ADMIN et TUTOR
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/quizzes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/quizzes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/quizzes/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TUTOR")

                        // ✅ PRIORITÉ 3 : Accès public en lecture seule (GET) pour les quiz et leurs composants
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/quizzes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/courses/**").permitAll()

                        // ✅ Sécurité globale pour tout le reste
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:4200"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}