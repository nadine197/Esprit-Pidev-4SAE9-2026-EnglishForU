import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Course {
  courseid: number;
  id: string;
  title: string;
  description: string;
  duration: string;
  creator: number;
  image: '📚',
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private apiUrl = "http://localhost:8056/api/courses";

  constructor(private http: HttpClient) {}

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.apiUrl}/all`);
  }

getCourseById(courseId: number): Observable<Course> {
  return this.http.get<Course>(`${this.apiUrl}/getCourseById/${courseId}`);
}
}