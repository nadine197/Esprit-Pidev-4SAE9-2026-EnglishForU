import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class FlouciService {
  private apiUrl = `${environment.gatewayUrl}/api/flouci`;

  constructor(private http: HttpClient) {}

  create(amountTnd: number) {
    return this.http.post<{ link: string; payment_id: string }>(`${this.apiUrl}/create`, amountTnd);
  }

  verify(paymentId: string) {
    return this.http.get<boolean>(`${this.apiUrl}/verify`, { params: { paymentId } });
  }
}