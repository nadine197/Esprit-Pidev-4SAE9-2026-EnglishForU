package tn.spring.packagee.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers(HttpMethod.GET, "/api/packages/active", "/api/packages/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/promos/validate").permitAll()

                        // admin-only
                        .requestMatchers(HttpMethod.POST, "/api/promos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/packages/**").hasRole("ADMIN")

                        // authenticated users
                        .requestMatchers("/api/subscriptions/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}