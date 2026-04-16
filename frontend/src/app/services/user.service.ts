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

  private getHeaders() {
  const token = localStorage.getItem('token');
  return new HttpHeaders().set('Authorization', `Bearer ${token}`); // <-- Espace ici !
}

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admins`, { headers: this.getHeaders() });
  }

  // Dans user.service.ts

createUser(user: any): Observable<any> {
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  
  // Appelle l'endpoint ADMIN -> /api/users/create-user
  return this.http.post(`${this.apiUrl}/create-user`, user, { headers });
}

  // 3. UPDATE (Matches @PutMapping("/{id}"))
  updateUser(id: string, user: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, user, { headers: this.getHeaders() });
  }

  // 4. BLOCK (Matches @PutMapping("/block/{id}"))
  blockUser(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/block/${id}`, {}, { headers: this.getHeaders() });
  }

  // 5. UNBLOCK (Matches @PutMapping("/unblock/{id}"))
  unblockUser(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/unblock/${id}`, {}, { headers: this.getHeaders() });
  }

  // 6. DELETE (Add this if you want to use the delete button)
  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  // Dans UserService
getAllStudents(): Observable<any> {
  return this.http.get(`${this.apiUrl}/role/STUDENT`, { headers: this.getHeaders() });
}

getAllTutors(): Observable<any> {
  return this.http.get(`${this.apiUrl}/role/TUTOR`, { headers: this.getHeaders() });
}

getAllAdmins(): Observable<any> {
  return this.http.get(`${this.apiUrl}/role/ADMIN`, { headers: this.getHeaders() });
}
}