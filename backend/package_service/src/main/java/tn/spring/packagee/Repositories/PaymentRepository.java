package tn.spring.packagee.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TargetType;


import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);
}