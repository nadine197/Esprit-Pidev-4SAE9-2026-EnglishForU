import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient) {}

  // Helper pour récupérer les headers avec le Token JWT
  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

getAllAdmins(): Observable<any[]> {
  return this.http.get<any[]>(`${this.resolveBaseUrl()}/api/users/admins`, { headers: this.getHeaders() });
}

getAllStudents(): Observable<any[]> {
  return this.http.get<any[]>(`${this.resolveBaseUrl()}/api/users/students`, { headers: this.getHeaders() });
}

getAllTutors(): Observable<any[]> {
  return this.http.get<any[]>(`${this.resolveBaseUrl()}/api/users/tutors`, { headers: this.getHeaders() });
}
  // 4. Créer un utilisateur (Admin action)
  createUser(user: any): Observable<any> {
    return this.http.post(`${this.resolveBaseUrl()}/api/users/create-user`, user, { headers: this.getHeaders() });
  }

  // 5. Mettre à jour un utilisateur
  updateUser(id: string, user: any): Observable<any> {
    return this.http.put<any>(`${this.resolveBaseUrl()}/api/users/${id}`, user, { headers: this.getHeaders() });
  }

  // 6. Bloquer un utilisateur
  blockUser(id: string): Observable<any> {
    return this.http.put(`${this.resolveBaseUrl()}/api/users/block/${id}`, {}, { headers: this.getHeaders() });
  }

  // 7. Débloquer un utilisateur
  unblockUser(id: string): Observable<any> {
    return this.http.put(`${this.resolveBaseUrl()}/api/users/unblock/${id}`, {}, { headers: this.getHeaders() });
  }

  // 8. Supprimer un utilisateur
  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.resolveBaseUrl()}/api/users/${id}`, { headers: this.getHeaders() });
  }

  private resolveBaseUrl(): string {
    const gateway = environment.gatewayUrl?.trim();
    if (environment.useDirectBackend) {
      return environment.backendUrl;
    }

    return gateway || environment.backendUrl;
  }
}