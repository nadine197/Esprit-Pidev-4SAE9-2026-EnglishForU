package tn.spring.user.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tn.spring.user.Models.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;    // Access token: 30 minutes
    public static final long ACCESS_TOKEN_TTL_MS = 30L * 60 * 1000;

    // Refresh token: 1 day OR 30 days
    public static final long REFRESH_TTL_1_DAY_MS = 24L * 60 * 60 * 1000;
    public static final long REFRESH_TTL_7_DAYS_MS = 7L * 24 * 60 * 60 * 1000;

    public static final long COOKIE_1_DAY_SECONDS = 24L * 60 * 60;
    public static final long COOKIE_7_DAYS_SECONDS = 7L * 24 * 60 * 60;
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean isTokenExpired(String token) {
        return  extractExpiration(token).before (new Date());
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(Map<String,Object> extraClaims, UserDetails userdetails,long expiresIn) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userdetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiresIn) )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact() ;

    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        return generateToken(claims, user, ACCESS_TOKEN_TTL_MS);
    }

    public String generateRefreshToken(User user, long refreshTtlMs) {
        // Usually fewer claims for refresh token is fine
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, user, refreshTtlMs);
    }
    public void addRefreshCookie(HttpServletResponse response, String refreshToken, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax") // use "None" + Secure if cross-site + withCredentials
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    public boolean isRefreshToken(String token) {
        try {
            Object type = extractClaim(token, c -> c.get("type"));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private Claims extractallClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractallClaims(token);
        return claimsResolver.apply(claims);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
