import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { environment } from '../../../../environments/environment';
import { loadStripe, Stripe, StripeElements } from '@stripe/stripe-js';
import * as L from 'leaflet';

const iconDefault = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-event-detail',
  templateUrl: './event-detail.html'
})
export class EventDetailComponent implements OnInit, AfterViewInit, OnDestroy {
  event: any = null;
  participants: any[] = [];
  pendingParticipants: any[] = [];
  loading = true;
  activeSection = 'about';

  private mapInstance: L.Map | null = null;

  showPaymentModal = false;
  paymentLoading = false;
  cardReady = false;
  paymentError = '';
  paymentSuccess = '';
  passCode = '';

  private stripe: Stripe | null = null;
  private stripeElements: StripeElements | null = null;
  private clientSecret = '';
  private paymentIntentId = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEvent(id);
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    if (this.mapInstance) {
      this.mapInstance.remove();
      this.mapInstance = null;
    }
  }

  loadEvent(id: number) {
    this.loading = true;
    this.eventService.getEventById(id).subscribe({
      next: data => {
        this.event = data;
        this.loadParticipants(id);
        if (data.isClubPresident) {
          this.loadPendingParticipants(id);
        }
        this.loading = false;
        setTimeout(() => this.initDetailMap(), 150);
      },
      error: () => { this.loading = false; }
    });
  }

  private initDetailMap(): void {
    if (!this.event?.latitude || !this.event?.longitude) return;
    const el = document.getElementById('detailMap');
    if (!el) return;
    if (this.mapInstance) {
      this.mapInstance.remove();
      this.mapInstance = null;
    }
    this.mapInstance = L.map('detailMap', { zoomControl: true, scrollWheelZoom: false })
      .setView([this.event.latitude, this.event.longitude], 15);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.mapInstance);
    L.marker([this.event.latitude, this.event.longitude])
      .addTo(this.mapInstance)
      .bindPopup(this.event.locationName || this.event.title)
      .openPopup();
  }

  loadParticipants(id: number) {
    this.eventService.getParticipants(id).subscribe(data => this.participants = data);
  }

  loadPendingParticipants(id: number) {
    this.eventService.getPendingParticipants(id).subscribe(data => this.pendingParticipants = data);
  }

  participate() {
    this.eventService.participate(this.event.id).subscribe({
      next: () => this.loadEvent(this.event.id),
      error: (err: any) => alert(err.error?.message || 'Failed to participate')
    });
  }

  async openPaymentModal() {
    this.paymentError = '';
    this.paymentSuccess = '';
    this.passCode = '';
    this.cardReady = false;
    this.showPaymentModal = true;
    this.paymentLoading = true;

    try {
      this.stripe = await loadStripe(environment.stripePublishableKey);
    } catch (e) {
      this.paymentError = 'Stripe failed to load. Please try again.';
      this.paymentLoading = false;
      return;
    }

    if (!this.stripe) {
      this.paymentError = 'Stripe failed to load. Check your connection.';
      this.paymentLoading = false;
      return;
    }

    const userEmail = this.getUserEmail();
    this.eventService.createPaymentIntent(this.event.id, userEmail).subscribe({
      next: (res) => {
        this.clientSecret = res.clientSecret;
        this.paymentIntentId = res.paymentIntentId;
        this.paymentLoading = false;
        this.mountPaymentElement();
      },
      error: (err) => {
        this.paymentError = err.error?.error || 'Failed to initialise payment. Try again.';
        this.paymentLoading = false;
      }
    });
  }

  private mountPaymentElement() {
    let attempts = 0;
    const maxAttempts = 10;

    const tryMount = () => {
      attempts++;
      const el = document.getElementById('card-element');

      if (!el) {
        if (attempts < maxAttempts) {
          setTimeout(tryMount, 100 * attempts);
        } else {
          this.paymentError = 'Failed to load payment form. Please close and try again.';
        }
        return;
      }

      if (!this.stripe) {
        this.paymentError = 'Stripe not loaded. Please refresh the page.';
        return;
      }

      el.innerHTML = '';

      this.stripeElements = this.stripe.elements({
        clientSecret: this.clientSecret,
        appearance: {
          theme: 'stripe',
          variables: {
            colorPrimary: '#059669',
            colorText: '#1e293b',
            fontFamily: '"Inter", Arial, sans-serif',
            borderRadius: '12px'
          }
        }
      });

      const paymentElement = this.stripeElements.create('payment' as any, {
        wallets: { applePay: 'never', googlePay: 'never' }
      } as any);

      paymentElement.mount('#card-element');

      paymentElement.on('ready', () => {
        this.cardReady = true;
      });

      paymentElement.on('change', (event: any) => {
        if (event.complete) {
          this.paymentError = '';
        }
      });
    };

    setTimeout(tryMount, 50);
  }

  closePaymentModal() {
    if (this.stripeElements) {
      try {
        const el = document.getElementById('card-element');
        if (el) el.innerHTML = '';
      } catch (_) {}
      this.stripeElements = null;
    }
    this.clientSecret = '';
    this.paymentIntentId = '';
    this.cardReady = false;
    this.paymentLoading = false;
    this.paymentError = '';
    this.paymentSuccess = '';
    this.showPaymentModal = false;
  }

  private retryWithFreshIntent(): void {
    if (this.stripeElements) {
      try {
        const el = document.getElementById('card-element');
        if (el) el.innerHTML = '';
      } catch (_) {}
      this.stripeElements = null;
    }
    this.clientSecret = '';
    this.paymentIntentId = '';
    this.cardReady = false;
    const userEmail = this.getUserEmail();
    this.eventService.createPaymentIntent(this.event.id, userEmail).subscribe({
      next: (res) => {
        this.clientSecret = res.clientSecret;
        this.paymentIntentId = res.paymentIntentId;
        this.mountPaymentElement();
      },
      error: () => {}
    });
  }

  async confirmPayment() {
    if (!this.stripe || !this.stripeElements) return;
    this.paymentLoading = true;
    this.paymentError = '';

    try {
      const result = await (this.stripe as any).confirmPayment({
        elements: this.stripeElements,
        redirect: 'if_required'
      });

      if (result.error) {
        this.paymentError = result.error.message || 'Payment failed. Please try again.';
        this.paymentLoading = false;
        this.retryWithFreshIntent();
        return;
      }

      let userLat: number | null = null;
      let userLng: number | null = null;

      try {
        const pos: GeolocationPosition = await new Promise((resolve, reject) =>
          navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 3000 })
        );
        userLat = pos.coords.latitude;
        userLng = pos.coords.longitude;
      } catch (_) {}

      const userName = this.getUserName();
      const userEmail = this.getUserEmail();

      this.eventService.confirmPayment(this.event.id, {
        paymentIntentId: this.paymentIntentId,
        latitude: userLat,
        longitude: userLng,
        userName,
        userEmail
      }).subscribe({
        next: (res) => {
          this.passCode = res.passCode;
          this.paymentSuccess = 'Payment successful! Your pass has been sent to ' + userEmail;
          this.paymentLoading = false;
          this.loadEvent(this.event.id);
        },
        error: (err) => {
          this.paymentError = err.error?.error || 'Registration failed after payment.';
          this.paymentLoading = false;
        }
      });
    } catch (error: any) {
      this.paymentError = error.message || 'Payment processing failed. Please try again.';
      this.paymentLoading = false;
    }
  }

  private getUserEmail(): string {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user).email : '';
  }

  private getUserName(): string {
    const user = localStorage.getItem('user');
    if (!user) return '';
    const u = JSON.parse(user);
    const parts = [u.name, u.lastName].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : (u.email || '');
  }

  acceptParticipant(participationId: number) {
    this.eventService.acceptParticipant(this.event.id, participationId).subscribe(() => {
      this.loadEvent(this.event.id);
    });
  }

  rejectParticipant(participationId: number) {
    this.eventService.rejectParticipant(this.event.id, participationId).subscribe(() => {
      this.loadEvent(this.event.id);
    });
  }

  deleteEvent() {
    if (confirm('Are you sure you want to delete this event?')) {
      this.eventService.deleteEvent(this.event.id).subscribe(() => {
        this.router.navigate(['/student/clubs', this.event.clubId]);
      });
    }
  }

  get isPastEvent(): boolean {
    if (!this.event?.eventDate) return false;
    return new Date(this.event.eventDate) < new Date();
  }

  get userParticipated(): boolean {
    return this.event?.participationStatus === 'ACCEPTED';
  }
}
