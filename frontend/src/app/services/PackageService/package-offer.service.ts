import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { from, Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  PackageOfferResponse,
  CreatePackageOfferRequest,
  AddPackageItemRequest
} from 'src/app/models/package.models';

@Injectable({ providedIn: 'root' })
export class PackageOfferService {
  private apiUrl = `${environment.gatewayUrl}/api/packages`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // ADMIN: create package
  createPackage(payload: CreatePackageOfferRequest): Observable<PackageOfferResponse> {
    return this.http.post<PackageOfferResponse>(this.apiUrl, payload, { headers: this.getHeaders() });
  }

  // ADMIN: get all packages (active + inactive)
  getAllPackages(): Observable<PackageOfferResponse[]> {
    return this.http.get<PackageOfferResponse[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  // ADMIN: update package (backend expects PackageOffer entity; use Partial to be flexible)
  updatePackage(id: number, payload: Partial<PackageOfferResponse>): Observable<PackageOfferResponse> {
    return this.http.put<PackageOfferResponse>(`${this.apiUrl}/${id}`, payload, { headers: this.getHeaders() });
  }

    GetById(id: number): Observable<PackageOfferResponse> {
    return this.http.get<PackageOfferResponse>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
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
  addItem(packageId: number, payload: AddPackageItemRequest): Observable<PackageOfferResponse> {
    return this.http.post<PackageOfferResponse>(`${this.apiUrl}/${packageId}/items`, payload, { headers: this.getHeaders() });
  }

  // PUBLIC / STUDENT: list active
  getActivePackages(): Observable<PackageOfferResponse[]> {
    return this.http.get<PackageOfferResponse[]>(`${this.apiUrl}/active`);
  }

  // PUBLIC / STUDENT: search
  searchPackages(q: string): Observable<PackageOfferResponse[]> {
    return this.http.get<PackageOfferResponse[]>(`${this.apiUrl}/search`, {
      headers: this.getHeaders(),
      params: { q }
    });
  }
}