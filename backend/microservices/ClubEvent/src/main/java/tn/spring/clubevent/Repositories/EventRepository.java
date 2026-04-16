package tn.spring.clubevent.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.clubevent.Models.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByClubId(Long clubId);
    List<Event> findByClubIdIn(List<Long> clubIds);
}

