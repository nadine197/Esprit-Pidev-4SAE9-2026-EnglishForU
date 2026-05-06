package tn.spring.discussion.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forward le JWT de la requête entrante vers les appels Feign inter-services.
 */
public class FeignConfig {

    @Bean
    public RequestInterceptor forwardAuthorizationInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                var attrs = RequestContextHolder.getRequestAttributes();
                if (!(attrs instanceof ServletRequestAttributes servletAttrs)) return;
                HttpServletRequest request = servletAttrs.getRequest();
                String auth = request.getHeader("Authorization");
                if (auth != null && !auth.isBlank()) {
                    template.header("Authorization", auth);
                }
            }
        };
    }
}
