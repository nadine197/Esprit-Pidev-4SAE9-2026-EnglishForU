import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DiscussionService {
  private apiUrl = `${environment.gatewayUrl}/api/discussions`;

  constructor(private http: HttpClient) {}

  // Pour l'admin : récupérer tous les groupes
  getAllGroups(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/groups/all`);
  }

  // Pour Student/Tutor : récupérer seulement leurs groupes
  getMyGroups(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/groups/user/${userId}`);
  }

  createGroup(group: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/groups`, group);
  }

  updateGroup(id: string, group: any): Observable<any> {
  return this.http.put(`${this.apiUrl}/groups/${id}`, group);
}

deleteGroup(id: string): Observable<any> {
  return this.http.delete(`${this.apiUrl}/groups/${id}`);
}

// Ajoute cette méthode dans DiscussionService
getMyGroupsByEmail(email: string): Observable<any> {
  // On appelle ton endpoint Gateway (8090)
  return this.http.get<any[]>(`${this.apiUrl}/groups/user/${email}`);
}
}