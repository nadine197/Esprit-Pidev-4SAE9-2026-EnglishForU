import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {Course} from "../features/courses/models/courses";
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CourseService {

  private api = 'http://localhost:8084/api/courses';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Course[]> {
    return this.http.get<Course[]>(this.api);
  }

  getById(id: number) {
    return this.http.get<Course>(`${this.api}/${id}`);
  }

  create(course: Course) {
    return this.http.post(this.api, course);
  }

  update(id: number, course: Course) {
    return this.http.put(`${this.api}/${id}`, course);
  }

  delete(id: number) {
    return this.http.delete(`${this.api}/${id}`);
  }
  sortByDuration(){
    return this.http.get<Course[]>(`${this.api}/sort/duration`);
  }
  search(keyword: string){
    return this.http.get<Course[]>(`${this.api}/search?keyword=${keyword}`);
  }
  downloadPdf(){
    return this.http.get(`${this.api}/pdf`,{
      responseType:'blob'
    });
  }
  downloadExcel(){
    return this.http.get(`${this.api}/excel`,{
      responseType:'blob'
    });
  }
}
