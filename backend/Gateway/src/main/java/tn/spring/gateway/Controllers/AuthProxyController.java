package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthProxyController {

    private final ProxyForwarder proxy;

    public AuthProxyController(ProxyForwarder proxy) {
        this.proxy = proxy;
    }

    private static final String USER_AUTH_BASE = "http://localhost:8081/api/auth";

    @PostMapping("/register-client")
    public ResponseEntity<String> registerClient(@RequestBody Map<String, Object> body,
                                                 HttpServletRequest req) {
        return proxy.forward(USER_AUTH_BASE + "/register-client", HttpMethod.POST, body, req);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, Object> body,
                                        HttpServletRequest req) {
        return proxy.forward(USER_AUTH_BASE + "/login", HttpMethod.POST, body, req);
    }

    @PostMapping("/ForgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody String login,
                                                 HttpServletRequest req) {
        // If your user service mapping is "/ForgotPassword/" keep the slash, otherwise remove it
        return proxy.forward(USER_AUTH_BASE + "/ForgotPassword/", HttpMethod.POST, login, req);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest req) {
        return proxy.forward(USER_AUTH_BASE + "/logout", HttpMethod.POST, null, req);
    }

    @PostMapping("/google")
    public ResponseEntity<String> google(@RequestBody Map<String, Object> body,
                                         HttpServletRequest req) {
        return proxy.forward(USER_AUTH_BASE + "/google", HttpMethod.POST, body, req);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest req) {
        return proxy.forward(USER_AUTH_BASE + "/refresh", HttpMethod.POST, null, req);
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> confirmReset(@RequestBody Map<String, Object> body,
                                               HttpServletRequest req) {
        return proxy.forward(USER_AUTH_BASE + "/reset-password/confirm", HttpMethod.POST, body, req);
    }
}