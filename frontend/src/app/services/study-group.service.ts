import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StudyGroup, MarkedDates, StudyGroupStatus } from '../features/study-groups/models/study-group';

@Injectable({ providedIn: 'root' })
export class StudyGroupService {

  private api = 'http://localhost:8084/api/study-groups';
  private headers = new HttpHeaders({
    'Authorization': 'Basic ' + btoa('admin:admin123')
  });

  constructor(private http: HttpClient) {}

  // ── CRUD ──────────────────────────────────────────────────────
  getAll(): Observable<StudyGroup[]> {
    return this.http.get<StudyGroup[]>(this.api);
  }

  getById(id: number): Observable<StudyGroup> {
    return this.http.get<StudyGroup>(`${this.api}/${id}`);
  }

  create(group: StudyGroup): Observable<StudyGroup> {
    return this.http.post<StudyGroup>(this.api, group);
  }

  update(id: number, group: StudyGroup): Observable<StudyGroup> {
    return this.http.put<StudyGroup>(`${this.api}/${id}`, group);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  // ── Calendrier ────────────────────────────────────────────────
  getByDate(date: Date): Observable<StudyGroup[]> {
    const params = new HttpParams().set('date', this.formatDate(date));
    return this.http.get<StudyGroup[]>(`${this.api}/calendar/by-date`, { params });
  }

  getByMonth(year: number, month: number): Observable<StudyGroup[]> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    return this.http.get<StudyGroup[]>(`${this.api}/calendar/by-month`, { params });
  }

  getMarkedDates(year: number, month: number): Observable<MarkedDates> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    return this.http.get<MarkedDates>(`${this.api}/calendar/marked-dates`, { params });
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.api}/stats`);
  }

  formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  getStatusLabel(status: StudyGroupStatus): string {
    const map: Record<StudyGroupStatus, string> = {
      ACTIVE:    'Actif',
      COMPLETED: 'Terminé',
      PLANNED:   'Planifié',
      CANCELLED: 'Annulé'
    };
    return map[status] ?? status;
  }

  getStatusColor(status: StudyGroupStatus): string {
    const map: Record<StudyGroupStatus, string> = {
      ACTIVE:    '#1D9E75',
      PLANNED:   '#185FA5',
      COMPLETED: '#534AB7',
      CANCELLED: '#E24B4A'
    };
    return map[status] ?? '#888780';
  }

  getCapacityPercent(group: StudyGroup): number {
    const count = group.studentsIds?.length ?? 0;
    return group.maxCapacity > 0
      ? Math.round((count / group.maxCapacity) * 100)
      : 0;
  }

  getCapacityColor(group: StudyGroup): string {
    const pct = this.getCapacityPercent(group);
    if (pct >= 90) return '#E24B4A';
    if (pct >= 60) return '#BA7517';
    return '#1D9E75';
  }
  search(filters: {
    name?:     string;
    level?:    string;
    status?:   string;
    location?: string;
    courseId?: number;
  }): Observable<StudyGroup[]> {

    let params = new HttpParams();

    if (filters.name?.trim())     params = params.set('name',     filters.name.trim());
    if (filters.level?.trim())    params = params.set('level',    filters.level.trim());
    if (filters.status?.trim())   params = params.set('status',   filters.status.trim());
    if (filters.location?.trim()) params = params.set('location', filters.location.trim());
    if (filters.courseId != null) params = params.set('courseId', filters.courseId.toString());

    return this.http.get<StudyGroup[]>(`${this.api}/search`, { params });
  }
  // Ajouter dans StudyGroupService

  getAuditLog(groupId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/${groupId}/audit`);
  }

  getRevisionTypeLabel(type: string): string {
    const map: Record<string, string> = {
      INSERT: 'Créé',
      UPDATE: 'Modifié',
      DELETE: 'Supprimé'
    };
    return map[type] ?? type;
  }

  getRevisionTypeColor(type: string): string {
    const map: Record<string, string> = {
      INSERT: '#1D9E75',
      UPDATE: '#2563eb',
      DELETE: '#E24B4A'
    };
    return map[type] ?? '#888780';
  }
  chat(message: string, groupId?: number): Observable<{ reply: string }> {
    let params = new HttpParams().set('message', message);
    if (groupId) params = params.set('groupId', groupId.toString());
    return this.http.post<{ reply: string }>(
      `${this.api}/chatbot`, null, { params }
    );
  }

}
