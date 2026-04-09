import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Content } from '../features/contents/models/content';
import {Course} from "../features/courses/models/courses";

@Injectable({
  providedIn: 'root'
})
export class ContentService {

  private api = 'http://localhost:8084/api/contents';

  constructor(private http: HttpClient) {}

  // GET ALL
  getAll(): Observable<Content[]> {
    return this.http.get<Content[]>(this.api);
  }

  // GET BY ID
  getById(id: number): Observable<Content> {
    return this.http.get<Content>(`${this.api}/${id}`);
  }

  // CREATE
  create(content: Content): Observable<Content> {
    return this.http.post<Content>(this.api, content);
  }

  // UPDATE
  update(id: number, content: Content): Observable<Content> {
    return this.http.put<Content>(`${this.api}/${id}`, content);
  }

  // DELETE
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
  search(keyword: string){
    return this.http.get<Content[]>(`${this.api}/search?keyword=${keyword}`);
  }
  downloadPdf(){
    return this.http.get(`${this.api}/pdf`,{
      responseType:'blob'
    });
  }
  getStatsByType(){
    return this.http.get<any[]>(`${this.api}/stats/type`);
  }
  downloadHistory(){

    return this.http.get(this.api + "/history/txt",{
      responseType:'blob'
    });

  }

}
