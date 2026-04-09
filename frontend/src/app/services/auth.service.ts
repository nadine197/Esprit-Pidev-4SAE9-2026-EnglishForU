import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment'; 

// 1. AJOUTER 'export' ici pour que AdminLayoutComponent puisse l'utiliser
export interface User {
  name: string;
  lastName: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) { }

  login(credentials: any): Observable<any> {
  return this.http.post<any>(`${this.resolveBaseUrl()}/api/auth/login`, credentials).pipe(
    tap((res: any) => {
      if (res && res.token) {
        localStorage.setItem('token', res.token);
        // Save the user object so the Guards and Components can see the Role
        localStorage.setItem('user', JSON.stringify(res.user)); 
      }
    })
  );
}

  // 2. AJOUTER cette méthode
  // In auth.service.ts
getUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

  // 3. AJOUTER cette méthode
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  signup(userData: any): Observable<any> {
    return this.http.post(`${this.resolveBaseUrl()}/api/auth/register-client`, userData);
  }

  private resolveBaseUrl(): string {
    const gateway = environment.gatewayUrl?.trim();
    if (environment.useDirectBackend) {
      return environment.backendUrl;
    }

    return gateway || environment.backendUrl;
  }
}