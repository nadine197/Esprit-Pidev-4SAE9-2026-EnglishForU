import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';

interface Purchase {
  id: number;
  eventId: number;
  eventTitle: string;
  eventDate: string;
  eventLocation: string;
  eventImageUrl: string;
  clubName: string;
  passCode: string;
  amountPaid: number;
  paidAt: string;
  status: string;
  stripePaymentIntentId: string;
  qrCodeBase64?: string;
}

@Component({
  selector: 'app-my-tickets',
  templateUrl: './my-tickets.html'
})
export class MyTicketsComponent implements OnInit {
  purchases: Purchase[] = [];
  loading = true;
  error = '';
  downloadingPass: number | null = null;
  downloadingReceipt: number | null = null;
  openingPass: number | null = null;
  openingReceipt: number | null = null;

  qrModalPurchase: Purchase | null = null;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.loadPurchases();
  }

  loadPurchases(): void {
    this.loading = true;
    this.error = '';

    this.eventService.getMyPurchases().subscribe({
      next: (data) => {
        this.purchases = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.error || 'Failed to load purchases';
        this.loading = false;
      }
    });
  }

  openPass(eventId: number): void {
    this.openingPass = eventId;
    this.eventService.getPassUrl(eventId).subscribe({
      next: (res) => {
        window.open(res.url, '_blank');
        this.openingPass = null;
      },
      error: (err) => {
        console.error('Failed to open pass:', err);
        this.openingPass = null;
      }
    });
  }

  downloadPass(eventId: number, passCode: string): void {
    this.downloadingPass = eventId;
    this.eventService.downloadPass(eventId).subscribe({
      next: (blob) => {
        const pdfBlob = new Blob([blob], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(pdfBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `EventPass-${passCode}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => window.URL.revokeObjectURL(url), 1000);
        this.downloadingPass = null;
      },
      error: (err) => {
        console.error('Failed to download pass:', err);
        this.downloadingPass = null;
      }
    });
  }

  openReceipt(eventId: number): void {
    this.openingReceipt = eventId;
    this.eventService.getReceiptUrl(eventId).subscribe({
      next: (res) => {
        window.open(res.url, '_blank');
        this.openingReceipt = null;
      },
      error: (err) => {
        console.error('Failed to open receipt:', err);
        this.openingReceipt = null;
      }
    });
  }

  downloadReceipt(eventId: number, txId: string): void {
    this.downloadingReceipt = eventId;
    this.eventService.downloadReceipt(eventId).subscribe({
      next: (blob) => {
        const pdfBlob = new Blob([blob], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(pdfBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Receipt-${txId ? txId.substring(0, 20) : 'receipt'}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => window.URL.revokeObjectURL(url), 1000);
        this.downloadingReceipt = null;
      },
      error: (err) => {
        console.error('Failed to download receipt:', err);
        this.downloadingReceipt = null;
      }
    });
  }

  showQR(purchase: Purchase): void {
    this.qrModalPurchase = purchase;
  }

  closeQR(): void {
    this.qrModalPurchase = null;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'TBD';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isUpcoming(dateStr: string): boolean {
    if (!dateStr) return true;
    return new Date(dateStr) > new Date();
  }

  get upcomingPurchases(): Purchase[] {
    return this.purchases.filter(p => this.isUpcoming(p.eventDate));
  }

  get pastPurchases(): Purchase[] {
    return this.purchases.filter(p => !this.isUpcoming(p.eventDate));
  }
}

