package tn.spring.quiz.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import tn.spring.quiz.Enums.UserRole;

import java.util.List;
import java.util.UUID;


@Entity(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    private String name;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;
    private String prefix;

    @Column(unique = true)
    private String phone;

    @Embedded
    private Address address;

    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // ✅ CORRIGÉ
    @OneToMany
    @JoinColumn(name = "creator_id")
    private List<Course> courses;
}