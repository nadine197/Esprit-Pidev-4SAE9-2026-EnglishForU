package tn.spring.discussion.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;

@Component
public class JwtUserContextResolver {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthenticatedUser resolveRequired(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_HEADER_MISSING");
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_MISSING");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(buildSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            Object roleClaim = claims.get("role");
            String role = roleClaim == null ? "STUDENT" : roleClaim.toString().trim().toUpperCase();

            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_USER_MISSING");
            }

            return new AuthenticatedUser(email.trim().toLowerCase(), role);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID");
        }
    }

    private SecretKey buildSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
