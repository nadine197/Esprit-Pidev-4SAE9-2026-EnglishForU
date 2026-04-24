package tn.spring.user.Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tn.spring.user.DTOs.AuthenticationRequest;
import tn.spring.user.DTOs.AuthenticationResponse;
import tn.spring.user.DTOs.GoogleAuthenticationRequest;
import tn.spring.user.DTOs.RegisterClientRequest;

public interface AuthService {

    AuthenticationResponse registerClient(RegisterClientRequest request);

    AuthenticationResponse login(AuthenticationRequest request, HttpServletResponse response, HttpServletRequest requestip);

    void ForgotPassword(String login);

    AuthenticationResponse loginWithGoogle(GoogleAuthenticationRequest request, HttpServletResponse response);

    AuthenticationResponse refresh(String refreshToken, HttpServletResponse response);

    void confirmReset(String token, String newPassword);
}