import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private apiUrl = `${environment.gatewayUrl}/api/appointments`;

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // Admin: Get all appointments
  getAppointments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/all`, { headers: this.getHeaders() });
  }

  // Inside appointment.service.ts

acceptAppointment(id: string): Observable<any> {
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  
  // L'URL doit correspondre au @PutMapping("/{id}/accept") du Java
  return this.http.put(`${this.apiUrl}/${id}/accept`, {}, { headers });
}

  // Admin: Add an availability slot
  addSlot(slot: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/slots`, slot, { headers: this.getHeaders() });
  }

  // Public: Book a test
  bookTest(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/book`, data);
  }

  cancelAppointment(id: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/cancel`, {}, { headers: this.getHeaders() });
  }

  // src/app/services/appointment.service.ts

// src/app/services/appointment.service.ts

// Dans appointment.service.ts
completeAppointment(id: string, result: string, score: string, cheatCount: number): Observable<any> {
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

  // ON ENCODE POUR LES ESPACES ET LE SLASH
  const resEnc = encodeURIComponent(result);
  const scoreEnc = encodeURIComponent(score);

  // Construction de l'URL propre
  const url = `${this.apiUrl}/${id}/complete?result=${resEnc}&score=${scoreEnc}&cheatCount=${cheatCount}`;

  return this.http.put(url, {}, { headers });
}

  rescheduleAppointment(id: string, newDate: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/reschedule?newDate=${newDate}`, {}, { headers: this.getHeaders() });
  }

  getAvailableSlots(): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/available-slots`);
}

getAppointmentsPaged(page: number, size: number, search: string, sortField: string, sortDir: string, 
                     status: string, location: string, suspicious: boolean): Observable<any> {
  
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

  // On construit l'URL avec les nouveaux filtres
  let url = `${this.apiUrl}/all?page=${page}&size=${size}&search=${search}&sort=${sortField},${sortDir}`;
  
  if (status) url += `&status=${status}`;
  if (location) url += `&locationType=${location}`;
  if (suspicious) url += `&suspicious=true`;

  return this.http.get<any>(url, { headers });
}


verifyAccess(email: string, code: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-access`, { email, code });
}


}