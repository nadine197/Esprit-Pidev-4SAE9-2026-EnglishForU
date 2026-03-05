import { Component, OnInit } from '@angular/core';
import { PaymentService } from 'src/app/services/PackageService/payment.service';
import { PaymentResponse, PaymentStatus } from 'src/app/models/payment.models';

@Component({
  selector: 'app-payment-management',
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.css']
})
export class PaymentManagementComponent implements OnInit {
  payments: PaymentResponse[] = [];
  loading = false;

  // filters
  filterStatus: '' | PaymentStatus = '';
  filterProvider: '' | 'CASH' | 'STRIPE' | 'FLOUCI' = '';

  // update status modal
  showStatusModal = false;
  selectedPayment: PaymentResponse | null = null;
  newStatus: PaymentStatus = 'PENDING';

  constructor(private paymentService: PaymentService) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments() {
    this.loading = true;

    this.paymentService.getAllPayments(
    
    ).subscribe({
      next: (data) => {
        // optional: newest first
        this.payments = (data || []).slice().sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  applyFilters() {
    this.loadPayments();
  }

  resetFilters() {
    this.filterStatus = '';
    this.filterProvider = '';
    this.loadPayments();
  }

  openStatusModal(p: PaymentResponse) {
    this.selectedPayment = p;
    this.newStatus = p.status;
    this.showStatusModal = true;
  }

  saveStatus() {
    if (!this.selectedPayment) return;

    this.paymentService.updatePaymentStatus(this.selectedPayment.id, this.newStatus).subscribe({
      next: (updated) => {
        const idx = this.payments.findIndex(x => x.id === updated.id);
        if (idx !== -1) this.payments[idx] = updated;
        this.showStatusModal = false;
        this.selectedPayment = null;
      }
    });
  }

  deletePayment(p: PaymentResponse) {
    if (p.status !== 'PENDING') {
      alert('Only PENDING payments can be deleted.');
      return;
    }
    if (!confirm(`Delete payment #${p.id}?`)) return;

    this.paymentService.deletePayment(p.id).subscribe({
      next: () => this.loadPayments()
    });
  }
}