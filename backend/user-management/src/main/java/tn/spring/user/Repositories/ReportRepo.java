package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Enums.ReportStatus;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.User;

import java.util.List;

public interface ReportRepo extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByCreatedAtDesc();
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);
    List<Report> findByCreatedByUserOrderByCreatedAtDesc(User createdByUser);
}
