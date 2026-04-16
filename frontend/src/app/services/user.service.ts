import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.gatewayUrl}/api/users`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admins`, { headers: this.getHeaders() });
  }

  getAllStudents(): Observable<any> {
    return this.http.get(`${this.apiUrl}/students`, { headers: this.getHeaders() });
  }

  getAllTutors(): Observable<any> {
    return this.http.get(`${this.apiUrl}/tutors`, { headers: this.getHeaders() });
  }

  getAllAdmins(): Observable<any> {
    return this.http.get(`${this.apiUrl}/admins`, { headers: this.getHeaders() });
  }

  createUser(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/create-user`, user, { headers: this.getHeaders() });
  }

  updateUser(id: string, user: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, user, { headers: this.getHeaders() });
  }

  blockUser(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/block/${id}`, {}, { headers: this.getHeaders() });
  }

  unblockUser(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/unblock/${id}`, {}, { headers: this.getHeaders() });
  }

  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}