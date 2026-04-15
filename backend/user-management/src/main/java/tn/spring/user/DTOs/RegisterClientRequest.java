package tn.spring.user.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterClientRequest {
    private String email;
    private String password;
    private String name;
    private String lastName;
    private String phone;
    private String prefix;
    private String fullName;
    private String role;
}
