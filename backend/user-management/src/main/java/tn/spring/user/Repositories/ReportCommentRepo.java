package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.ReportComment;

import java.util.List;

public interface ReportCommentRepo extends JpaRepository<ReportComment, Long> {
    List<ReportComment> findByReportOrderByCreatedAtAsc(Report report);
}
