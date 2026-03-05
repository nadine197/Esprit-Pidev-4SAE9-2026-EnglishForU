import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

type ResultStatus = 'success' | 'failed' | 'pending';

@Component({
  selector: 'app-payment-result',
  templateUrl: './payment-result.component.html'
})
export class PaymentResultComponent implements OnInit {
  status: ResultStatus = 'pending';
  paymentId: number | null = null;
  reason: string | null = null;

  title = '';
  subtitle = '';
  emoji = '⏳';

  constructor(private route: ActivatedRoute, public router: Router) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const rawStatus = (params.get('status') || 'pending').toLowerCase();
      this.paymentId = params.get('id') ? Number(params.get('id')) : null;
      this.reason = params.get('reason');

      if (rawStatus === 'success' || rawStatus === 'failed' || rawStatus === 'pending') {
        this.status = rawStatus;
      } else {
        this.status = 'pending';
      }

      this.applyCopy();
    });
  }

  private applyCopy() {
    if (this.status === 'success') {
      this.emoji = '✅';
      this.title = 'Payment successful';
      this.subtitle = 'Your package is activated. You can start learning right away.';
      return;
    }

    if (this.status === 'failed') {
      this.emoji = '❌';
      this.title = 'Payment failed';
      this.subtitle = this.reason
        ? `Reason: ${this.reason}`
        : 'We couldn’t complete your payment. Please try again.';
      return;
    }

    this.emoji = '⏳';
    this.title = 'Payment pending';
    this.subtitle = 'We are waiting for confirmation. If you paid online, refresh in a moment.';
  }

  goPricing() {
    this.router.navigate(['/main']);
  }

  retry() {
    // If you want to go back to checkout, pass packageId too in query params earlier.
    // For now, go pricing.
    this.router.navigate(['/pricing']);
  }
}