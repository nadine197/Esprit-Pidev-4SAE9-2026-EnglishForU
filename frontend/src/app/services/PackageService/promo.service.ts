import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  PromoCode,
  CreatePromoCodeRequest,
  ApplyPromoRequest,
  ApplyPromoResponse
} from 'src/app/models/promo.models';

@Injectable({ providedIn: 'root' })
export class PromoService {
  private apiUrl = `${environment.gatewayUrl}/api/promos`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // ADMIN: create promo
  createPromo(payload: CreatePromoCodeRequest): Observable<void> {
    return this.http.post<void>(this.apiUrl, payload, { headers: this.getHeaders() });
  }

  // ADMIN: get all
  getAllPromos(): Observable<PromoCode[]> {
    return this.http.get<PromoCode[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  // ADMIN: update (your backend currently expects PromoCode entity in update)
  updatePromo(id: number, payload: Partial<PromoCode>): Observable<PromoCode> {
    return this.http.put<PromoCode>(`${this.apiUrl}/${id}`, payload, { headers: this.getHeaders() });
  }

  // ADMIN: delete
  deletePromo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  // ADMIN: enable
  enablePromo(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/enable`, {}, { headers: this.getHeaders() });
  }

  // ADMIN: disable
  disablePromo(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/disable`, {}, { headers: this.getHeaders() });
  }

  // STUDENT/ADMIN: validate promo
  validatePromo(payload: ApplyPromoRequest): Observable<ApplyPromoResponse> {
    return this.http.post<ApplyPromoResponse>(`${this.apiUrl}/validate`, payload, { headers: this.getHeaders() });
  }
}