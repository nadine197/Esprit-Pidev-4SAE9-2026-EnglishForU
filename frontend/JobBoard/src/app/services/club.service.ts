import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClubService {
  private apiUrl = `${environment.gatewayUrl}/api/clubs`;

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

  getAllClubs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  getClubById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  getMyClubs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/my?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  createClub(club: any): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}?userId=${this.getUserEmail()}&userName=${encodeURIComponent(this.getUserName())}`,
      club,
      { headers: this.getHeaders() }
    );
  }

  updateClub(id: number, club: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}?userId=${this.getUserEmail()}`, club, { headers: this.getHeaders() });
  }

  deleteClub(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}?userId=${this.getUserEmail()}`, { headers: this.getHeaders() });
  }

  joinClub(clubId: number): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/${clubId}/join?userId=${this.getUserEmail()}&userName=${encodeURIComponent(this.getUserName())}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  acceptMember(clubId: number, membershipId: number): Observable<any> {
    return this.http.put<any>(
      `${this.apiUrl}/${clubId}/members/${membershipId}/accept?userId=${this.getUserEmail()}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  rejectMember(clubId: number, membershipId: number): Observable<any> {
    return this.http.put<any>(
      `${this.apiUrl}/${clubId}/members/${membershipId}/reject?userId=${this.getUserEmail()}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  getMembers(clubId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${clubId}/members`, { headers: this.getHeaders() });
  }

  getPendingMembers(clubId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${clubId}/members/pending`, { headers: this.getHeaders() });
  }
}

