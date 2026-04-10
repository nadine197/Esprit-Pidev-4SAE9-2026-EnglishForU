package tn.spring.user.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterClientRequest {
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    @Size(max = 255, message = "EMAIL_TOO_LONG")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, max = 128, message = "WEAK_PASSWORD")
    private String password;

    @NotBlank(message = "NAME_REQUIRED")
    @Size(max = 100, message = "NAME_TOO_LONG")
    private String name;

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @Size(max = 100, message = "LAST_NAME_TOO_LONG")
    private String lastName;

    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = "^[0-9\\-\\s]{6,20}$", message = "INVALID_PHONE")
    private String phone;

    @NotBlank(message = "PREFIX_REQUIRED")
    @Pattern(regexp = "^\\+?[0-9]{1,5}$", message = "INVALID_PHONE_PREFIX")
    private String prefix;

    @NotBlank(message = "ROLE_REQUIRED")
    private String role;
}
