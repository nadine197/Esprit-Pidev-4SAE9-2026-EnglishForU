import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Answer {
  id: number;
  text: string;
  correct: boolean;
}

export interface QuizRecommendationAnswer {
  text: string;
  correct: boolean;
}

export interface QuizRecommendation {
  courseTitle: string;
  recommendationReason: string;
  questionText: string;
  answers: QuizRecommendationAnswer[];
}

export interface Question {
  id: number;
  text: string;
  answers: Answer[];
}

export interface NewAnswerPayload {
  text: string;
  correct: boolean;
}

export interface NewQuestionPayload {
  text: string;
  answers: NewAnswerPayload[];
}

export interface CertificateGenerationPayload {
  quizId: number;
  studentId: string;
  studentName: string;
  studentEmail: string;
}

export interface Quiz {
  id: number;
  title: string;
  passingScore: number;
  questions: Question[];
  
}
export interface Course {
  id: number;
  title: string;
  passingScore: number;
  quiz: Quiz[];
  
}
@Injectable({
  providedIn: 'root'
})
export class QuizService {

  private apiUrl = "http://localhost:8056/api/quizzes";
  private attemptUrl = "http://localhost:8056/api/quiz-attempts";

  constructor(private http: HttpClient) {}

  getAllQuizzes(): Observable<Quiz[]> {
    return this.http.get<Quiz[]>(this.apiUrl);
  }

  getQuizById(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${id}`);
  }

  createQuiz(quiz: Quiz): Observable<Quiz> {
    return this.http.post<Quiz>(this.apiUrl, quiz);
  }

  updateQuiz(id: number, quiz: Quiz): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.apiUrl}/${id}`, quiz);
  }

  deleteQuiz(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  submitQuiz(payload: any): Observable<any> {
    return this.http.post(`${this.attemptUrl}/submit`, payload);
  }
  getQuizQuestions(quizId: number): Observable<Question[]> {
  return this.http.get<Question[]>(`${this.apiUrl}/${quizId}/questions`);
}
getAnswersByQuestion(questionId: number): Observable<Answer[]> {
  return this.http.get<Answer[]>(`${this.apiUrl}/questions/${questionId}/answers`);
  
}
getQuizzesByCourse(courseId: number): Observable<Quiz[]> {
  return this.http.get<Quiz[]>(`${this.apiUrl}/course/${courseId}`);
}
getQuizStatus(quizId: number, studentId: string): Observable<any> {
  return this.http.get(
    `${this.attemptUrl}/status?quizId=${quizId}&studentId=${studentId}`
  );
}
updateQuestion(questionId: number, question: Question): Observable<Question> {
  return this.http.put<Question>(`${this.apiUrl}/question/${questionId}`, question);
}

updateAnswer(answerId: number, answer: Answer): Observable<Answer> {
  return this.http.put<Answer>(`${this.apiUrl}/answers/${answerId}`, answer);
}

deleteAnswer(answerId: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/answers/${answerId}`);
}

addQuestion(quizId: number, question: NewQuestionPayload): Observable<Question> {
  return this.http.post<Question>(`${this.apiUrl}/quizAddQuestion/${quizId}`, question);
}


addAnswer(questionId: number, answer: NewAnswerPayload): Observable<Answer> {
  return this.http.post<Answer>(`${this.apiUrl}/${questionId}/answers`, answer);
}


deleteQuestion(questionId: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/question/${questionId}`);
}

addQuiz(courseId: number, quiz: { title: string }): Observable<Quiz> {
  const quizToSend = { ...quiz, passingScore: 70, questions: [] };
  return this.http.post<Quiz>(`${this.apiUrl}/course/${courseId}`, quizToSend);
}

generateCourseCertificate(payload: CertificateGenerationPayload): Observable<HttpResponse<Blob>> {
  return this.http.post(`${this.attemptUrl}/certificates/generate`, payload, {
    observe: 'response',
    responseType: 'blob'
  });
}

getQuizRecommendation(quizId: number): Observable<QuizRecommendation> {
  return this.http.post<QuizRecommendation>(`${this.apiUrl}/${quizId}/recommendations`, {});
}
}
