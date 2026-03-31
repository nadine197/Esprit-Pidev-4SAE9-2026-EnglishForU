import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

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

  login(credentials: any): Observable<any> {
  return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
    tap((res: any) => {
      if (res && res.token) {
        localStorage.setItem('token', res.token);

        localStorage.setItem('user', JSON.stringify(res.user));
      }
    })
  );
}

getUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  signup(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register-client`, userData);
  }
}
