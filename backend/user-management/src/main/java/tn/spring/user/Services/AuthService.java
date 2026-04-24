package tn.spring.user.Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.AuthenticationRequest;
import tn.spring.user.DTOs.AuthenticationResponse;
import tn.spring.user.DTOs.GoogleAuthenticationRequest;
import tn.spring.user.DTOs.RegisterClientRequest;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Exceptions.BadRequestException;
import tn.spring.user.Models.PasswordResetToken;
import tn.spring.user.Models.Student;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.PasswordResetTokenRepo;
import tn.spring.user.Repositories.StudentRepos;
import tn.spring.user.Repositories.UserRepos;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final  PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepos userRepos;
    private final JwtService jwtService;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final AuthenticationManager authenticationManager;
    private final StudentRepos studentRepos;


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isEmail(String login) {
        if (login == null || login.isBlank()) return false;
        return EMAIL_PATTERN.matcher(login.trim()).matches();
    }
    public AuthenticationResponse login(AuthenticationRequest request, HttpServletResponse response,
                                        HttpServletRequest requestip) {

        String loginKey = request.getLogin() != null ? request.getLogin().trim() : null;
        if (loginKey == null || loginKey.isEmpty() || !isEmail(loginKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LOGIN_NOT_FOUND");
        }

        // Find the user
        User user = userRepos.findByEmailIgnoreCase(loginKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        if (!user.isAccountNonLocked())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_BLOCKED");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginKey, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INCORRECT_CREDENTIALS");
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);

        long refreshTtlMs = request.isRememberMe()
                ? JwtService.REFRESH_TTL_7_DAYS_MS
                : JwtService.REFRESH_TTL_1_DAY_MS;

        long cookieMaxAgeSeconds = request.isRememberMe()
                ? JwtService.COOKIE_7_DAYS_SECONDS
                : JwtService.COOKIE_1_DAY_SECONDS;

        String refreshToken = jwtService.generateRefreshToken(user, refreshTtlMs);
        jwtService.addRefreshCookie(response, refreshToken, cookieMaxAgeSeconds);

        // --- FIX STARTS HERE: Create the User Details DTO ---
        AuthenticationResponse.UserDetailsDTO userDTO = AuthenticationResponse.UserDetailsDTO.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name()) // This sends "ADMIN" or "STUDENT" as a string
                .build();

        // Return the response WITH the user object
        return AuthenticationResponse.builder()
                .token(accessToken)
                .user(userDTO) // <--- THIS WAS MISSING
                .build();
    }


    public AuthenticationResponse registerStudent(RegisterClientRequest request) {
        String password = passwordEncoder.encode(request.getPassword());

        User user;

        // On vérifie le rôle envoyé dans le JSON de Postman
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            user = User.builder() // Simple User pour l'admin
                    .role(UserRole.ADMIN)
                    .build();
        } else {
            user = Student.builder() // Student pour les élèves
                    .role(UserRole.STUDENT)
                    .englishLevel("A1")
                    .learningGoal("General")
                    .dailyGoalMinutes(0)
                    .build();
        }

        // Champs communs
        user.setEmail(request.getEmail());
        user.setPassword(password);
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setPrefix(request.getPrefix());
        user.setActive(true);

        userRepos.save(user); // Utiliser userRepos au lieu de studentRepos pour la polyvalence

        return AuthenticationResponse.builder()
                .token(jwtService.generateAccessToken(user))
                .build();
    }


    @Transactional
    public void ForgotPassword(String email)  {
        var user = userRepos.findByEmailIgnoreCase(email).orElse(null);

        // ✅ always succeed (avoid email enumeration)
        if (user == null) return;

        // revoke old tokens
        passwordResetTokenRepo.revokeAllActiveByUserId(user.getId(), Instant.now());

        String raw = TokenUtil.randomToken();
        String sha = TokenUtil.sha256Hex(raw);

        PasswordResetToken t = new PasswordResetToken();
        t.setUserId(user.getId());
        t.setTokenSha256(sha);
        t.setCreatedAt(Instant.now());
        t.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));

        passwordResetTokenRepo.save(t);


    }
    public AuthenticationResponse refresh(String refreshToken, HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "NO_REFRESH_TOKEN");
        }

        // Ensure it is a refresh token (claim type=refresh)
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }

        String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }

        User user = userRepos.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        // Block refresh for locked/disabled accounts
        if (!user.isAccountNonLocked() ) {
            jwtService.clearRefreshCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_BLOCKED");
        }

        // Validate signature + expiration + subject
        if (!jwtService.isTokenValid(refreshToken, user)) {
            jwtService.clearRefreshCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_EXPIRED");
        }



        // ✅ Rotate refresh token (recommended)
        // Keep SAME remaining lifetime as current refresh token
        Date exp = jwtService.extractExpirationDate(refreshToken);
        long remainingMs = exp.getTime() - System.currentTimeMillis();

        // Defensive: if remaining time is too small, force re-login
        if (remainingMs < 30_000) { // <30 seconds
            jwtService.clearRefreshCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_EXPIRED");
        }
        // ✅ Issue new access token (30 minutes)
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, remainingMs);

        // Cookie max-age must match remaining lifetime (in seconds)
        long remainingSeconds = Math.max(1, remainingMs / 1000);

        jwtService.addRefreshCookie(response, newRefreshToken, remainingSeconds);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .build();
    }
    public AuthenticationResponse loginWithGoogle(
            GoogleAuthenticationRequest request,
            HttpServletResponse response
    ) {
        // 1) verify idToken
        var payload = googleTokenVerifierService.verify(request.getIdToken());

        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());

        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");

        // 2) find or create user
        User user = userRepos.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(givenName);
            user.setLastName(familyName);
            user.setActive(true);

            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // temp
            user.setRole(UserRole.STUDENT);

        }

        if (!user.isAccountNonLocked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_BLOCKED");

        }

        // 3) mark verified if Google says verified
        user = userRepos.save(user);

        // 4) generate access token (30 min)
        String accessToken = jwtService.generateAccessToken(user);


            String refreshToken = jwtService.generateRefreshToken(user, JwtService.REFRESH_TTL_7_DAYS_MS);
            jwtService.addRefreshCookie(response, refreshToken, JwtService.COOKIE_7_DAYS_SECONDS);

        AuthenticationResponse.UserDetailsDTO userDetails = AuthenticationResponse.UserDetailsDTO.builder()
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name()) // This will be "ADMIN" or "STUDENT"
                .build();

        return AuthenticationResponse.builder()
                .token(accessToken)
                .user(userDetails) // <--- Now we are sending the user data!
                .build();
    }


    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_REQUIRED");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD");
        }

        String sha = TokenUtil.sha256Hex(rawToken);

        PasswordResetToken t = passwordResetTokenRepo.findByTokenSha256(sha)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (t.getRevokedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_REVOKED");
        }
        if (t.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_USED");
        }
        if (Instant.now().isAfter(t.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED");
        }

        User user = userRepos.findById(t.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepos.save(user);

        // mark used
        t.setUsedAt(Instant.now());
        passwordResetTokenRepo.save(t);

        // optional: revoke any other outstanding tokens
        passwordResetTokenRepo.revokeAllActiveByUserId(user.getId(), Instant.now());
    }
}
