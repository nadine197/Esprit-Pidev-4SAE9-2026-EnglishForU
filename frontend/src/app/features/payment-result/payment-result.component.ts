import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from 'src/app/services/PackageService/payment.service';

type ResultStatus = 'success' | 'failed' | 'pending';
type Method = 'CASH' | 'STRIPE' | 'FLOUCI' | null;
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

  constructor(private route: ActivatedRoute, public router: Router,
    private paymentService: PaymentService
  ) {}


method: Method = null;
ngOnInit(): void {
  this.route.queryParamMap.subscribe(params => {
    const rawStatus = (params.get('status') || 'pending').toLowerCase();
    this.paymentId = params.get('id') ? Number(params.get('id')) : null;
    this.reason = params.get('reason');

    const m = (params.get('method') || '').toUpperCase();
    this.method = (m === 'CASH' || m === 'STRIPE' || m === 'FLOUCI') ? (m as any) : null;

    this.status = (rawStatus === 'success' || rawStatus === 'failed' || rawStatus === 'pending')
      ? (rawStatus as ResultStatus)
      : 'pending';

    this.applyCopy();
  });
}
private applyCopy() {
  // ✅ CASH pending special copy
  if (this.status === 'pending' && this.method === 'CASH') {
    this.emoji = '🧾';
    this.title = 'Cash payment pending';
    this.subtitle =
      'Please go to the nearest agency and pay using your Payment ID as reference. ' +
      'Your package will be activated after payment is verified.';
    return;
  }

  // normal success/failed/pending
  if (this.status === 'success') {
    this.emoji = '✅';
    this.title = 'Payment successful';
    this.subtitle = 'Your package is activated. You can start learning right away.';
    return;
  }

  if (this.status === 'failed') {
    this.emoji = '❌';
    this.title = 'Payment failed';
    this.subtitle = this.reason ? `Reason: ${this.reason}` : 'We couldn’t complete your payment. Please try again.';
    return;
  }

  this.emoji = '⏳';
  this.title = 'Payment pending';
  this.subtitle = 'We are waiting for confirmation. If you paid online, refresh in a moment.';
}
downloadVoucher() {
  const id = Number(this.route.snapshot.queryParamMap.get('id'));
  this.paymentService.downloadVoucher(id).subscribe(blob => {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `voucher-${id}.pdf`;
  a.click();
  window.URL.revokeObjectURL(url);
});
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