package tn.spring.course.Config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 1. Récupérer la requête HTTP entrante (celle du client vers Course)
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();

            // 2. Extraire le Header "Authorization" (qui contient le JWT Token Bearer)
            String authorizationHeader = request.getHeader("Authorization");


            if (authorizationHeader != null) {
                requestTemplate.header("Authorization", authorizationHeader);
            }
        }
    }
}
