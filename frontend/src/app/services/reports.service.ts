import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ReportCategory = 'BUG' | 'ISSUE' | 'FEATURE_REQUEST';
export type ReportSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ReportStatus = 'NEW' | 'TRIAGED' | 'IN_PROGRESS' | 'DONE' | 'CLOSED';

export interface ReportUserSummary {
  id: string;
  name: string;
  lastName: string;
  email: string;
}

export interface ReportTicket {
  id: number;
  title: string;
  category: ReportCategory;
  severity: ReportSeverity;
  status: ReportStatus;
  description: string;
  shortDescription: string;
  createdAt: string;
  updatedAt: string;
  createdBy: ReportUserSummary;
  assignedTo?: ReportUserSummary | null;
  stepsToReproduce?: string | null;
  expectedResult?: string | null;
  actualResult?: string | null;
  pageUrl?: string | null;
  userAgent?: string | null;
  appVersion?: string | null;
}

export interface CreateReportPayload {
  title: string;
  category: ReportCategory;
  severity: ReportSeverity;
  description: string;
  stepsToReproduce?: string;
  expectedResult?: string;
  actualResult?: string;
  pageUrl?: string;
  userAgent?: string;
  appVersion?: string;
}

export interface UpdateHelpdeskReportPayload {
  status?: ReportStatus;
  assignedToUserId?: string;
  assignToMe?: boolean;
  unassign?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ReportsService {

  constructor(private http: HttpClient) {}

  createReport(payload: CreateReportPayload): Observable<ReportTicket> {
    return this.http.post<ReportTicket>(`${this.resolveBaseUrl()}/api/reports`, payload, {
      headers: this.getHeaders()
    });
  }

  getMyReports(): Observable<ReportTicket[]> {
    return this.http.get<ReportTicket[]>(`${this.resolveBaseUrl()}/api/reports/mine`, {
      headers: this.getHeaders()
    });
  }

  getHelpdeskReports(status?: ReportStatus): Observable<ReportTicket[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<ReportTicket[]>(`${this.resolveBaseUrl()}/api/helpdesk/reports`, {
      headers: this.getHeaders(),
      params
    });
  }

  updateHelpdeskReport(id: number, payload: UpdateHelpdeskReportPayload): Observable<ReportTicket> {
    return this.http.patch<ReportTicket>(`${this.resolveBaseUrl()}/api/helpdesk/reports/${id}`, payload, {
      headers: this.getHeaders()
    });
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private resolveBaseUrl(): string {
    const gateway = environment.gatewayUrl?.trim();
    if (environment.useDirectBackend) {
      return environment.backendUrl;
    }

    return gateway || environment.backendUrl;
  }
}
