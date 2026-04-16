package tn.spring.packagee.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserPublicDTO {
    private String name;
    private String lastName;
    private UUID id;
}