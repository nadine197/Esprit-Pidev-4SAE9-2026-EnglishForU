package tn.spring.user.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.AuthenticationRequest;
import tn.spring.user.DTOs.AuthenticationResponse;
import tn.spring.user.DTOs.GoogleAuthenticationRequest;
import tn.spring.user.DTOs.RegisterClientRequest;
import tn.spring.user.DTOs.ResetPasswordConfirmRequest;
import tn.spring.user.Services.AuthService;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthService authenticationService;

    public AuthController(AuthService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register-client")
    public ResponseEntity<AuthenticationResponse> registerClient(@RequestBody RegisterClientRequest request) {
        AuthenticationResponse response = authenticationService.registerClient(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response,
            HttpServletRequest requestip
    ) {
        AuthenticationResponse authResponse = authenticationService.login(request, response, requestip);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/ForgotPassword")
    public ResponseEntity<Void> forgotPassword(@RequestBody String login) {
        authenticationService.ForgotPassword(login);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> loginWithGoogle(
            @RequestBody GoogleAuthenticationRequest request,
            HttpServletResponse response
    ) {
        AuthenticationResponse authResponse = authenticationService.loginWithGoogle(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthenticationResponse authResponse = authenticationService.refresh(refreshToken, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ResetPasswordConfirmRequest req) {
        if (!Objects.equals(req.getNewPassword(), req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PASSWORDS_DO_NOT_MATCH");
        }

        authenticationService.confirmReset(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok().build();
    }
}