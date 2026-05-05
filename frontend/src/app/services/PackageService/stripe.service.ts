import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StripeService {

  private API_URL = `${environment.gatewayUrl}/api/stripe`;

  constructor(private http: HttpClient) {}

createCheckoutSession(paymentId: number, amount: number, packageName: string): Observable<any> {
  return this.http.post(
    `${this.API_URL}/create-checkout-session`,
    {
      paymentId,
      amount,
      packageName
    }
  );
}
}