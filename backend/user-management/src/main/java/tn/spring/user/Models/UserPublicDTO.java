package tn.spring.user.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPublicDTO {
    private String name;
    private String lastName;
    private UUID id;
}