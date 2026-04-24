package tn.spring.user.Services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.user.DTOs.AuthenticationRequest;
import tn.spring.user.DTOs.AuthenticationResponse;
import tn.spring.user.DTOs.GoogleAuthenticationRequest;
import tn.spring.user.DTOs.RegisterClientRequest;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Exceptions.BadRequestException;
import tn.spring.user.Exceptions.ConflictException;
import tn.spring.user.Exceptions.NotFoundException;
import tn.spring.user.Exceptions.UnauthorizedException;
import tn.spring.user.Models.PasswordResetToken;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.PasswordResetTokenRepo;
import tn.spring.user.Repositories.UserRepos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private static final String DEFAULT_PHONE_PREFIX = "+216";

    private final UserRepos userRepos;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Override
    public AuthenticationResponse registerClient(RegisterClientRequest request) {
        String email = normalizeEmail(request.getEmail());
        String phone = normalizePhone(request.getPhone());
        String prefix = normalizePrefix(request.getPrefix());
        UserRole role = parseRole(request.getRole());

        if (email == null || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("EMAIL_AND_PASSWORD_REQUIRED");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("NAME_REQUIRED");
        }
        if (phone == null) {
            throw new BadRequestException("PHONE_REQUIRED");
        }
        if (userRepos.findByEmailIgnoreCase(email).isPresent()) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS");
        }
        if (userRepos.findByPrefixAndPhone(prefix, phone).isPresent()) {
            throw new ConflictException("PHONE_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .lastName(normalizeLastName(request.getLastName()))
                .email(email)
                .password(request.getPassword())
                .phone(phone)
                .prefix(prefix)
                .role(role)
                .active(true)
                .build();

        User savedUser = userService.createStudentOrTutor(user);
        return buildAuthResponse(savedUser);
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest request, HttpServletResponse response, HttpServletRequest requestip) {
        if (request.getLogin() == null || request.getLogin().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("LOGIN_AND_PASSWORD_REQUIRED");
        }

        String login = request.getLogin().trim().toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("INVALID_CREDENTIALS");
        }

        User user = userRepos.findByEmailIgnoreCase(login)
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS"));

        long refreshTtl = request.isRememberMe()
                ? JwtService.REFRESH_TTL_7_DAYS_MS
                : JwtService.REFRESH_TTL_1_DAY_MS;
        long cookieMaxAge = request.isRememberMe()
                ? JwtService.COOKIE_7_DAYS_SECONDS
                : JwtService.COOKIE_1_DAY_SECONDS;

        String refreshToken = jwtService.generateRefreshToken(user, refreshTtl);
        jwtService.addRefreshCookie(response, refreshToken, cookieMaxAge);

        return buildAuthResponse(user);
    }

    @Override
    public void ForgotPassword(String login) {
        if (login == null || login.isBlank()) {
            throw new BadRequestException("LOGIN_REQUIRED");
        }

        User user = userRepos.findByEmailIgnoreCase(login.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        passwordResetTokenRepo.revokeAllActiveByUserId(user.getId(), Instant.now());

        String rawToken = TokenUtil.randomToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenSha256(TokenUtil.sha256Hex(rawToken));
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        passwordResetTokenRepo.save(token);
    }

    @Override
    public AuthenticationResponse loginWithGoogle(GoogleAuthenticationRequest request, HttpServletResponse response) {
        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            throw new BadRequestException("GOOGLE_TOKEN_REQUIRED");
        }

        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(request.getIdToken());
        String email = normalizeEmail(payload.getEmail());
        if (email == null) {
            throw new BadRequestException("GOOGLE_EMAIL_REQUIRED");
        }

        User user = userRepos.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    String fullName = payload.get("name") instanceof String value ? value : "";
                    String[] parts = fullName.trim().split("\\s+", 2);
                    RegisterClientRequest registerRequest = RegisterClientRequest.builder()
                            .email(email)
                            .password(TokenUtil.randomToken())
                            .name(parts.length > 0 ? parts[0] : "Google")
                            .lastName(parts.length > 1 ? parts[1] : "")
                            .phone("google-" + payload.getSubject())
                            .prefix(DEFAULT_PHONE_PREFIX)
                            .role(UserRole.STUDENT.name())
                            .build();
                    registerClient(registerRequest);
                    return userRepos.findByEmailIgnoreCase(email)
                            .orElseThrow(() -> new UnauthorizedException("GOOGLE_LOGIN_FAILED"));
                });

        String refreshToken = jwtService.generateRefreshToken(user, JwtService.REFRESH_TTL_1_DAY_MS);
        jwtService.addRefreshCookie(response, refreshToken, JwtService.COOKIE_1_DAY_SECONDS);

        return buildAuthResponse(user);
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("MISSING_REFRESH_TOKEN");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("INVALID_REFRESH_TOKEN");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userRepos.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("INVALID_REFRESH_TOKEN"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("EXPIRED_REFRESH_TOKEN");
        }

        String rotatedRefreshToken = jwtService.generateRefreshToken(user, JwtService.REFRESH_TTL_1_DAY_MS);
        jwtService.addRefreshCookie(response, rotatedRefreshToken, JwtService.COOKIE_1_DAY_SECONDS);

        return buildAuthResponse(user);
    }

    @Override
    public void confirmReset(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("TOKEN_AND_PASSWORD_REQUIRED");
        }

        PasswordResetToken resetToken = passwordResetTokenRepo.findByTokenSha256(TokenUtil.sha256Hex(token))
                .orElseThrow(() -> new NotFoundException("RESET_TOKEN_NOT_FOUND"));

        if (!resetToken.isActive()) {
            throw new UnauthorizedException("RESET_TOKEN_EXPIRED");
        }

        User user = userRepos.findById(resetToken.getUserId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepos.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepo.save(resetToken);
    }

    private AuthenticationResponse buildAuthResponse(User user) {
        return AuthenticationResponse.builder()
                .token(jwtService.generateAccessToken(user))
                .user(AuthenticationResponse.UserDetailsDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .prefix(user.getPrefix())
                        .phone(user.getPhone())
                        .build())
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return DEFAULT_PHONE_PREFIX;
        }
        return prefix.trim();
    }

    private String normalizeLastName(String lastName) {
        return lastName == null ? "" : lastName.trim();
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return UserRole.STUDENT;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("INVALID_ROLE");
        }
    }
}
