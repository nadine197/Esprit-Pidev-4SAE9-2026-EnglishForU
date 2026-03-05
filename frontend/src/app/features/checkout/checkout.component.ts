import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PackageOfferService } from 'src/app/services/PackageService/package-offer.service';
import { PackageOfferResponse } from 'src/app/models/package.models';
import { CreatePaymentRequest, PaymentMethod } from 'src/app/models/payment.models';
import { PaymentService } from 'src/app/services/PackageService/payment.service';
import { PromoService } from 'src/app/services/PackageService/promo.service';
import { ApplyPromoRequest, ApplyPromoResponse } from 'src/app/models/promo.models';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent implements OnInit {
  packageId!: number;
  pkg?: PackageOfferResponse;

  loadingPkg = false;
  loadingPay = false;
  error: string | null = null;

  selectedMethod: PaymentMethod | null = null;

  // created payment info
  paymentId: number | null = null;
  showProviderBox = false;

  // if your backend returns redirect url for stripe/flouci
  providerCheckoutUrl: string | null = null;

  // example: from auth/local storage
  studentId = 1;

   promoCode = '';
  promoLoading = false;
  promoError: string | null = null;

  promoApplied: ApplyPromoResponse | null = null;

  // computed numbers for display + payment
  amountOriginal = 0;
  discountAmount = 0;
  amountFinal = 0;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private packageService: PackageOfferService,
    private paymentService: PaymentService,
    private promoService: PromoService // ✅ add this
  ) {}

  ngOnInit(): void {
    this.packageId = Number(this.route.snapshot.paramMap.get('packageId'));
    this.loadPackage();
  }

  selectMethod(m: PaymentMethod) {
    this.selectedMethod = m;
    this.showProviderBox = false;
    this.providerCheckoutUrl = null;
    this.paymentId = null;
  }

   loadPackage() {
    this.loadingPkg = true;
    this.error = null;

    this.packageService.GetById(this.packageId).subscribe({
      next: (p) => {
        this.pkg = p;

        // ✅ init totals
        this.amountOriginal = Number(p.price);
        this.discountAmount = 0;
        this.amountFinal = this.amountOriginal;

        // reset promo state if package changes
        this.promoApplied = null;
        this.promoCode = '';
        this.promoError = null;

        this.loadingPkg = false;
      },
      error: () => {
        this.error = 'Package not found.';
        this.loadingPkg = false;
      }
    });
  }

  applyPromo() {
    if (!this.pkg) return;

    const code = (this.promoCode || '').trim();
    if (!code) {
      this.promoError = 'Enter a promo code.';
      return;
    }

    this.promoLoading = true;
    this.promoError = null;

    const payload: ApplyPromoRequest = {
      code,
      studentId: this.studentId,
      amountOriginal: this.amountOriginal
    };

    this.promoService.validatePromo(payload).subscribe({
      next: (res) => {
        this.promoApplied = res;

        if (!res.valid) {
          this.discountAmount = 0;
          this.amountFinal = this.amountOriginal;
          this.promoError = 'Invalid promo code.';
        } else {
          this.discountAmount = Number(res.discountAmount || 0);
          this.amountFinal = Number(res.amountFinal || (this.amountOriginal - this.discountAmount));
        }

        this.promoLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.promoError = 'Promo validation failed.';
        this.discountAmount = 0;
        this.amountFinal = this.amountOriginal;
        this.promoApplied = null;
        this.promoLoading = false;
      }
    });
  }

  clearPromo() {
    this.promoCode = '';
    this.promoApplied = null;
    this.promoError = null;
    this.discountAmount = 0;
    this.amountFinal = this.amountOriginal;
  }

  createPayment() {
    if (!this.pkg || !this.selectedMethod) return;

    this.loadingPay = true;
    this.error = null;

    const payload: CreatePaymentRequest = {
      studentId: this.studentId,
      targetType: 'PACKAGE',
      targetId: this.pkg.id,

      // ✅ keep original and discount separate
      amountOriginal: this.amountOriginal,
      discountAmount: this.discountAmount,

      paymentMethod: this.selectedMethod
    };

    this.paymentService.createPayment(payload).subscribe({
      next: (paymentRes) => {
        this.paymentId = paymentRes.id;

        if (this.selectedMethod === 'CASH') {
          this.router.navigate(['/payment-result'], {
            queryParams: { status: 'success', id: paymentRes.id }
          });
          return;
        }

        this.showProviderBox = true;
        this.providerCheckoutUrl = paymentRes.checkoutUrl || null;
        this.loadingPay = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Payment creation failed.';
        this.loadingPay = false;
      }
    });
  }


  payNow() {
    // If you have a redirect URL from backend, easiest:
    if (this.providerCheckoutUrl) {
      window.location.href = this.providerCheckoutUrl;
      return;
    }

    // Otherwise, you will call your provider init endpoint here (Stripe session create, Flouci init)
    // Then redirect to provider page. (Depends on your backend integration)
  }

  // Call these after provider returns (success/fail).
  confirmPayment(providerRef?: string) {
    if (!this.paymentId) return;

    this.paymentService.confirmPayment(this.paymentId, { providerRef }).subscribe({
      next: () => {
        this.router.navigate(['/payment-result'], {
          queryParams: { status: 'success', id: this.paymentId }
        });
      },
      error: () => {
        this.error = 'Payment confirmation failed.';
      }
    });
  }

  failPayment(reason: string) {
    if (!this.paymentId) return;

    this.paymentService.failPayment(this.paymentId, reason).subscribe({
      next: () => {
        this.router.navigate(['/payment-result'], {
          queryParams: { status: 'failed', id: this.paymentId }
        });
      },
      error: () => {
        this.error = 'Payment fail update failed.';
      }
    });
  }
}