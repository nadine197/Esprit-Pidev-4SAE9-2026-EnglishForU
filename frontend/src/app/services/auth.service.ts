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
  private apiUrl = `${environment.gatewayUrl}/api/auth`; 

  constructor(private http: HttpClient) { }

  private extractAccessToken(res: any): string | null {
    return res?.token ?? res?.accessToken ?? res?.jwt ?? null;
  }

  private extractRefreshToken(res: any): string | null {
    return res?.refreshToken ?? null;
  }

  login(credentials: any): Observable<any> {
  return this.http.post<any>(`${this.apiUrl}/login`, credentials, { withCredentials: true }).pipe(
    tap((res: any) => {
      const token = this.extractAccessToken(res);
      const refreshToken = this.extractRefreshToken(res);
      if (token) {
        localStorage.setItem('token', token);
        // Save the user object so the Guards and Components can see the Role
        localStorage.setItem('user', JSON.stringify(res.user)); 
      }
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
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

  refreshToken(): Observable<any> {
    const storedRefreshToken = localStorage.getItem('refreshToken');
    const body = storedRefreshToken ? { refreshToken: storedRefreshToken } : {};
    return this.http.post<any>(`${this.apiUrl}/refresh`, body, { withCredentials: true }).pipe(
      tap((res: any) => {
        const token = this.extractAccessToken(res);
        const refreshToken = this.extractRefreshToken(res);
        if (token) {
          localStorage.setItem('token', token);
        }
        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }
      })
    );
  }

  // 3. AJOUTER cette méthode
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  signup(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register-client`, userData);
  }
}
