package tn.spring.user.DTOs;

import lombok.Data;

@Data
public class ResetPasswordConfirmRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
