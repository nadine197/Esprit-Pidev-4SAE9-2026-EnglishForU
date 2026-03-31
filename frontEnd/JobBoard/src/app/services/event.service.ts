import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = `${environment.gatewayUrl}/api/events`;

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
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

  getAllEvents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  getEventsByClub(clubId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/club/${clubId}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  getEventById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  createEvent(event: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}?userId=${this.getUserEmail()}`, event, { headers: this.getHeaders() });
  }

  updateEvent(id: number, event: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}?userId=${this.getUserEmail()}`, event, { headers: this.getHeaders() });
  }

  deleteEvent(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  participate(eventId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/${eventId}/participate?userId=${this.getUserEmail()}&userName=${encodeURIComponent(this.getUserName())}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  acceptParticipant(eventId: number, participationId: number): Observable<any> {
    return this.http.put<any>(
      `${this.apiUrl}/${eventId}/participants/${participationId}/accept?userId=${this.getUserEmail()}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  rejectParticipant(eventId: number, participationId: number): Observable<any> {
    return this.http.put<any>(
      `${this.apiUrl}/${eventId}/participants/${participationId}/reject?userId=${this.getUserEmail()}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  getParticipants(eventId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${eventId}/participants`, { headers: this.getHeaders() });
  }

  getPendingParticipants(eventId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${eventId}/participants/pending`, { headers: this.getHeaders() });
  }

  createPaymentIntent(eventId: number, userEmail: string): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/${eventId}/payment/intent`,
      { userEmail },
      { headers: this.getHeaders() }
    );
  }

  confirmPayment(eventId: number, body: {
    paymentIntentId: string;
    latitude: number | null;
    longitude: number | null;
    userName: string;
    userEmail: string;
  }): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/${eventId}/payment/confirm?userId=${this.getUserEmail()}`,
      body,
      { headers: this.getHeaders() }
    );
  }

  getMyPurchases(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/my-purchases?userId=${this.getUserEmail()}`,
      { headers: this.getHeaders() }
    );
  }

  downloadPass(eventId: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${eventId}/pass?userId=${this.getUserEmail()}&view=false`,
      { headers: this.getHeaders(), responseType: 'blob' }
    );
  }

  openPass(eventId: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${eventId}/pass?userId=${this.getUserEmail()}&view=true`,
      { headers: this.getHeaders(), responseType: 'blob' }
    );
  }

  getPassUrl(eventId: number): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(
      `${this.apiUrl}/${eventId}/pass-url?userId=${this.getUserEmail()}`,
      { headers: this.getHeaders() }
    );
  }

  downloadReceipt(eventId: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${eventId}/receipt?userId=${this.getUserEmail()}&view=false`,
      { headers: this.getHeaders(), responseType: 'blob' }
    );
  }

  openReceipt(eventId: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${eventId}/receipt?userId=${this.getUserEmail()}&view=true`,
      { headers: this.getHeaders(), responseType: 'blob' }
    );
  }

  getReceiptUrl(eventId: number): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(
      `${this.apiUrl}/${eventId}/receipt-url?userId=${this.getUserEmail()}`,
      { headers: this.getHeaders() }
    );
  }
}

