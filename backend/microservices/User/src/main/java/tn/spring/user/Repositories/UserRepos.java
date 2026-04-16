package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Models.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepos extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPrefixAndPhone(String prefix, String phone);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
}

