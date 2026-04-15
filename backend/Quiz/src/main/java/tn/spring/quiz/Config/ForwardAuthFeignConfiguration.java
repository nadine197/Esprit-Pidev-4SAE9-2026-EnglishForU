package tn.spring.quiz.Config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Used only via {@code @FeignClient(configuration = ...)} so the incoming browser JWT is forwarded
 * to other microservices. A {@code @Configuration} class with this bean is not reliably picked up
 * by Feign unless referenced explicitly on each client.
 */
public class ForwardAuthFeignConfiguration {

    @Bean
    public RequestInterceptor forwardAuthorizationInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                var attributes = RequestContextHolder.getRequestAttributes();
                if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
                    return;
                }
                HttpServletRequest request = servletAttributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
