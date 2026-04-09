import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Subject, Observable } from 'rxjs';
import * as SockJS from 'sockjs-client';
import { Stomp, CompatClient } from '@stomp/stompjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private stompClient: CompatClient | null = null;
  private messageSource = new Subject<any>();
  public messages$ = this.messageSource.asObservable();

  // On crée un Subject spécifique pour le statut "Typing"
  private typingSource = new Subject<any>();

  private apiUrl = `http://localhost:8090/api/discussions`;

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // --- REST API ---
  getMessages(groupId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/groups/${groupId}/messages`, { headers: this.getHeaders() });
  }

  // --- WEBSOCKET LOGIC ---
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.stompClient?.connected) { resolve(); return; }

      const socket = new SockJS('http://localhost:8087/ws-chat');
      this.stompClient = Stomp.over(socket);
      this.stompClient.debug = () => {}; 

      this.stompClient.connect({}, () => {
        resolve();
      }, (err: any) => reject(err));
    });
  }

  // S'abonner aux messages d'un groupe
  subscribeToGroup(groupId: string) {
    this.stompClient?.subscribe(`/topic/group/${groupId}`, (payload) => {
      this.messageSource.next(JSON.parse(payload.body));
    });
  }

  // ✅ CETTE MÉTHODE MANQUAIT : S'abonner au statut "Typing"
  subscribeToTyping(groupId: string): Observable<any> {
    this.stompClient?.subscribe(`/topic/group/${groupId}/typing`, (payload) => {
      this.typingSource.next(JSON.parse(payload.body));
    });
    return this.typingSource.asObservable();
  }

  // Envoyer son propre statut "Typing"
  sendTypingStatus(groupId: string, userName: string, isTyping: boolean) {
    this.stompClient?.send(`/app/chat.typing/${groupId}`, {}, JSON.stringify({ userName, isTyping }));
  }

  sendMessage(groupId: string, msg: any) {
    this.stompClient?.send(`/app/chat.send/${groupId}`, {}, JSON.stringify(msg));
  }

  editMessage(groupId: string, msg: any) {
    this.stompClient?.send(`/app/chat.edit/${groupId}`, {}, JSON.stringify(msg));
  }

  deleteMessage(groupId: string, msg: any) {
    this.stompClient?.send(`/app/chat.delete/${groupId}`, {}, JSON.stringify(msg));
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.stompClient = null;
    }
  }
}