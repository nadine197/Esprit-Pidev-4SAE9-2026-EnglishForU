package tn.spring.gateway.Config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration



public class SecurityConfig {

    private final GatewayJwtFilter gatewayJwtFilter;

    public SecurityConfig(GatewayJwtFilter gatewayJwtFilter) {
        this.gatewayJwtFilter = gatewayJwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**",
                                "/api/packages/active").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

                        // ── Endpoints publics (auth) ──────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        // ── Lecture publique (GET sans token) ─────────────
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/courses",
                                "/api/courses/**",
                                "/api/contents",
                                "/api/contents/**",
                                "/api/study-groups",
                                "/api/study-groups/**"
                        ).permitAll()
                        // ── Toutes les autres requêtes → JWT obligatoire ──
                        .anyRequest().authenticated()
                )
                // Brancher le filtre JWT AVANT le filtre d'authentification standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));


        configuration.setAllowedMethods(List.of("GET", "PATCH","POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedMethods(List.of("GET", "PATCH","POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}

}

