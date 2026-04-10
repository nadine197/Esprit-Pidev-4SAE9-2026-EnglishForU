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

  login(credentials: any): Observable<any> {
  return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
    tap((res: any) => {
      if (res && res.token) {
        localStorage.setItem('token', res.token);
<<<<<<< HEAD
        // Save the user object so the Guards and Components can see the Role
        localStorage.setItem('user', JSON.stringify(res.user)); 
=======
        localStorage.setItem('user', JSON.stringify(res.user)); 
        if (res.user?.role) {
          localStorage.setItem('userRole', res.user.role);
          localStorage.setItem('ROLE', res.user.role);
        } else {
          localStorage.removeItem('userRole');
          localStorage.removeItem('ROLE');
        }
        if (res.user?.id) {
          localStorage.setItem('USER_ID', String(res.user.id));
        } else {
          localStorage.removeItem('USER_ID');
        }

>>>>>>> 21f8a6f (metier avancer + controle de saisie)
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
<<<<<<< HEAD
=======
    localStorage.removeItem('ROLE');
    localStorage.removeItem('USER_ID');
    localStorage.removeItem('userRole');
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  signup(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register-client`, userData);
  }
<<<<<<< HEAD
}
=======
}
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
