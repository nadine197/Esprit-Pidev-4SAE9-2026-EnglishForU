import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type NotificationType = 'REPORT_CREATED';

export interface AppNotification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  link: string;
  reportId?: number | null;
  createdAt: string;
  readAt?: string | null;
  read: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationsService {

  constructor(private http: HttpClient) {}

  list(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.resolveBaseUrl()}/api/notifications`, {
      headers: this.getHeaders()
    });
  }

  markRead(id: number): Observable<AppNotification> {
    return this.http.post<AppNotification>(`${this.resolveBaseUrl()}/api/notifications/${id}/read`, {}, {
      headers: this.getHeaders()
    });
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private resolveBaseUrl(): string {
    const gateway = environment.gatewayUrl?.trim();
    if (environment.useDirectBackend) {
      return environment.backendUrl;
    }

    return gateway || environment.backendUrl;
  }
}
