package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.ReportActivity;

import java.util.List;

public interface ReportActivityRepo extends JpaRepository<ReportActivity, Long> {
    List<ReportActivity> findByReportOrderByCreatedAtDesc(Report report);
}
