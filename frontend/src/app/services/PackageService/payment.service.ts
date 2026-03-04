import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private apiUrl = `${environment.gatewayUrl}/api/payments`;

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // create payment
  createPayment(payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, payload, { headers: this.getHeaders() });
  }

  // confirm payment
  confirmPayment(id: number, payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/confirm`, payload, { headers: this.getHeaders() });
  }

  // fail payment (reason as query param)
  failPayment(id: number, reason: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/fail`, null, {
      headers: this.getHeaders(),
      params: { reason }
    });
  }

  // get by id
  getPaymentById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  // list by student
  getPaymentsByStudent(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/student/${studentId}`, { headers: this.getHeaders() });
  }
}