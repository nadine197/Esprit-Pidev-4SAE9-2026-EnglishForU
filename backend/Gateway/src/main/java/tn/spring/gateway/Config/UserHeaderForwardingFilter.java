package tn.spring.gateway.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Order(1)
public class UserHeaderForwardingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String userId = (String) request.getAttribute("userId");
        String role   = (String) request.getAttribute("role");

        // Wrap only if attributes are present (i.e. token was already parsed)
        if (userId != null || role != null) {
            request = new HttpServletRequestWrapper(request) {
                private final Map<String, String> extraHeaders = new HashMap<>();

                {
                    if (userId != null) extraHeaders.put("x-user-id",   userId);
                    if (role   != null) extraHeaders.put("x-user-role", role);
                }

                @Override
                public String getHeader(String name) {
                    String val = extraHeaders.get(name.toLowerCase());
                    return val != null ? val : super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    String val = extraHeaders.get(name.toLowerCase());
                    if (val != null) return Collections.enumeration(List.of(val));
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    names.addAll(extraHeaders.keySet());
                    return Collections.enumeration(names);
                }
            };
        }

        chain.doFilter(request, servletResponse);
    }
}