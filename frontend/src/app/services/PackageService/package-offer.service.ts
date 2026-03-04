import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class PackageOfferService {
  private apiUrl = `${environment.gatewayUrl}/api/packages`;

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // ADMIN: create package
  createPackage(payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, payload, { headers: this.getHeaders() });
  }

  // ADMIN: get all packages (active + inactive)
  getAllPackages(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}`, { headers: this.getHeaders() });
  }

  // ADMIN: update package
  updatePackage(id: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payload, { headers: this.getHeaders() });
  }

  // ADMIN: enable package
  enablePackage(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/enable`, {}, { headers: this.getHeaders() });
  }

  // ADMIN: disable package (soft delete)
  disablePackage(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/disable`, {}, { headers: this.getHeaders() });
  }

  // ADMIN: add item
  addItem(packageId: number, payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${packageId}/items`, payload, { headers: this.getHeaders() });
  }

  // PUBLIC / STUDENT: list active
  getActivePackages(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/active`, { headers: this.getHeaders() });
  }

  // PUBLIC / STUDENT: search
  searchPackages(q: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/search`, {
      headers: this.getHeaders(),
      params: { q }
    });
  }
}