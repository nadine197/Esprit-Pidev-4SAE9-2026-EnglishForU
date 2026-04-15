package tn.spring.user.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private UserDetailsDTO user; // <--- Add this

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDetailsDTO {
        private String name;
        private String lastName;
        private String email;
        private String role;
    }
}
