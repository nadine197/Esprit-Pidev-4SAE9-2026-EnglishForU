package tn.spring.packagee.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.FlouciVerifyDTO;
import tn.spring.packagee.DTOs.ResponsePaymentFlouciDTO;
import tn.spring.packagee.Services.PaymentFlouciService;

import java.io.IOException;
import java.math.BigInteger;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flouci")
public class PaymentFlouciController {

    private final PaymentFlouciService flouciService;

    @PostMapping("/create")
    public ResponseEntity<ResponsePaymentFlouciDTO> createPayment(@RequestBody BigInteger amountTnd) throws IOException {
        BigInteger millimes = amountTnd.multiply(BigInteger.valueOf(1000)); // ✅
        return ResponseEntity.ok(flouciService.generatePayment(millimes));
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestParam String paymentId) throws Exception {
        return ResponseEntity.ok(flouciService.verifyPayment(paymentId));
    }
}