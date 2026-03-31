import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private apiUrl = `${environment.gatewayUrl}/api/feedback`;

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

  getEventFeedback(eventId: number): Observable<any> {
    const userId = this.getUserEmail();
    return this.http.get<any>(
      `${this.apiUrl}/event/${eventId}?userId=${userId}`,
      { headers: this.getHeaders() }
    );
  }

  getClubFeedback(clubId: number): Observable<any> {
    const userId = this.getUserEmail();
    return this.http.get<any>(
      `${this.apiUrl}/club/${clubId}?userId=${userId}`,
      { headers: this.getHeaders() }
    );
  }

  getEventStats(eventId: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/event/${eventId}/stats`,
      { headers: this.getHeaders() }
    );
  }

  getClubStats(clubId: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/club/${clubId}/stats`,
      { headers: this.getHeaders() }
    );
  }

  submitEventFeedback(eventId: number, rating: number, comment: string): Observable<any> {
    const userId = this.getUserEmail();
    const userName = this.getUserName();
    return this.http.post<any>(
      `${this.apiUrl}?userId=${userId}&userName=${encodeURIComponent(userName)}`,
      { targetType: 'EVENT', targetId: eventId, rating, comment },
      { headers: this.getHeaders() }
    );
  }

  submitClubFeedback(clubId: number, rating: number, comment: string): Observable<any> {
    const userId = this.getUserEmail();
    const userName = this.getUserName();
    return this.http.post<any>(
      `${this.apiUrl}?userId=${userId}&userName=${encodeURIComponent(userName)}`,
      { targetType: 'CLUB', targetId: clubId, rating, comment },
      { headers: this.getHeaders() }
    );
  }

  deleteFeedback(feedbackId: number): Observable<any> {
    const userId = this.getUserEmail();
    return this.http.delete<any>(
      `${this.apiUrl}/${feedbackId}?userId=${userId}`,
      { headers: this.getHeaders() }
    );
  }
}
