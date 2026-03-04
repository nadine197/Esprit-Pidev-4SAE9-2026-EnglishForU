package tn.spring.packagee.Services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.CreateSubscriptionRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.DTOs.SubscriptionResponse;
import tn.spring.packagee.Entities.PackageOffer;
import tn.spring.packagee.Entities.PackageSubscription;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Enum.SubscriptionStatus;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PackageOfferRepository;
import tn.spring.packagee.Repositories.PackageSubscriptionRepository;
import tn.spring.packagee.Repositories.PaymentRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubscriptionService {

    private final PackageOfferRepository offerRepo;
    private final PackageSubscriptionRepository subRepo;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepo;
    public SubscriptionService(PackageOfferRepository offerRepo, PackageSubscriptionRepository subRepo,
                               PaymentService paymentService, PaymentRepository paymentRepo) {
        this.offerRepo = offerRepo;
        this.subRepo = subRepo;
        this.paymentService = paymentService;
        this.paymentRepo = paymentRepo;
    }

    public SubscriptionResponse createSubscription(CreateSubscriptionRequest req) {
        PackageOffer offer = offerRepo.findById(req.getPackageOfferId())
                .orElseThrow(() -> new NotFoundException("PackageOffer not found: " + req.getPackageOfferId()));
       Payment pay = paymentRepo.findById(req.getPaymentId()).orElseThrow(
               ()->  new NotFoundException("Payment not found: " + req.getPaymentId())
       );

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(offer.getDurationDays());

        PackageSubscription s = new PackageSubscription();
        s.setStudentId(req.getStudentId());
        s.setPackageOfferId(offer.getId());
        s.setStartDate(start);
        s.setEndDate(end);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setPayment(pay );

        s = subRepo.save(s);
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listByStudent(Long studentId) {
        return subRepo.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SubscriptionResponse toResponse(PackageSubscription s) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(s.getId());
        r.setStudentId(s.getStudentId());
        r.setPackageOfferId(s.getPackageOfferId());
        r.setStartDate(s.getStartDate());
        r.setEndDate(s.getEndDate());
        r.setStatus(s.getStatus());
        r.setRemainingUses(s.getRemainingUses());
        r.setPayment( paymentService.toResponse(s.getPayment()) );
        return r;
    }
}