package tn.spring.user.DTOs;

import lombok.Data;
import tn.spring.user.Enums.UserRole;

@Data
public class ChangeRoleRequest {
    private UserRole newRole;
}
