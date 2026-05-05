import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StudentEvaluation {
  id: number;
  studentId: string;
  studentName: string;
  studentEmail: string;
  quizAttemptId: number | null;
  quizId: number | null;
  quizTitle: string | null;
  scoreSnapshot: number | null;
  passedSnapshot: boolean | null;
  title: string;
  feedback: string;
  strengths: string | null;
  areasToImprove: string | null;
  recommendedActions: string | null;
  rating: number | null;
  evaluatorEmail: string | null;
  evaluatorRole: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StudentEvaluationRequest {
  studentId: string;
  studentName: string;
  studentEmail: string;
  quizAttemptId: number | null;
  title: string;
  feedback: string;
  strengths: string | null;
  areasToImprove: string | null;
  recommendedActions: string | null;
  rating: number | null;
}

export interface MotivationSuggestionRequest {
  studentId: string;
  studentName: string;
  studentEmail: string;
  englishLevel: string | null;
  learningGoal: string | null;
  quizAttemptId: number | null;
  title: string | null;
  rating: number | null;
  feedback: string | null;
  strengths: string | null;
  areasToImprove: string | null;
  recommendedActions: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class StudentEvaluationService {
  private readonly evaluationUrl = 'http://localhost:8090/api/evaluations';

  constructor(private http: HttpClient) {}

  getEvaluations(studentId?: string): Observable<StudentEvaluation[]> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId);
    }
    return this.http.get<StudentEvaluation[]>(this.evaluationUrl, { params });
  }

  getMyEvaluations(): Observable<StudentEvaluation[]> {
    return this.http.get<StudentEvaluation[]>(`${this.evaluationUrl}/mine`);
  }

  createEvaluation(payload: StudentEvaluationRequest): Observable<StudentEvaluation> {
    return this.http.post<StudentEvaluation>(this.evaluationUrl, payload);
  }

  updateEvaluation(id: number, payload: StudentEvaluationRequest): Observable<StudentEvaluation> {
    return this.http.put<StudentEvaluation>(`${this.evaluationUrl}/${id}`, payload);
  }

  deleteEvaluation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.evaluationUrl}/${id}`);
  }

  getMotivationSuggestions(payload: MotivationSuggestionRequest): Observable<any> {
    return this.http.post<any>(
      `${this.evaluationUrl}/motivation-suggestions`,
      payload
    );
  }
}
