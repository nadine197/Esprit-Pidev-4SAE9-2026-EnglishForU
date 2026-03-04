import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CertificateRequest {
  courseId: number;
  userName: string;
  userEmail: string;
  finalScore: number;
}
@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  private apiUrl = 'http://localhost:8090/api/certificates';

  constructor(private http: HttpClient) {}

  // ✅ récupérer certificat
  getCertificate(courseId: number, studentId: string) {
    return this.http.get<any>(
      `${this.apiUrl}/by-course/${courseId}/student/${studentId}`
    );
  }

  // ✅ générer + envoyer
  generateAndSend(request: CertificateRequest): Observable<Blob> {
    return this.http.post(
      `${this.apiUrl}/generate-and-send`,
      request,
      { responseType: 'blob' }
    );
  }
}
