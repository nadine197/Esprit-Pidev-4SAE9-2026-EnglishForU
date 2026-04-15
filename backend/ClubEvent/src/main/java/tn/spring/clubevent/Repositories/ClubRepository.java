package tn.spring.clubevent.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.clubevent.Models.Club;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    List<Club> findByPresidentId(String presidentId);
    List<Club> findByNameContainingIgnoreCase(String name);
}

