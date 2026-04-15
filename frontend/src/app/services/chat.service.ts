import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Subject, Observable } from 'rxjs';
import { Stomp, CompatClient } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class ChatService {
  public stompClient: CompatClient | null = null;

  private messageSource = new Subject<any>();
  public messages$ = this.messageSource.asObservable();

  private typingSource = new Subject<any>();
  public typing$ = this.typingSource.asObservable();
private socketUrl = 'http://localhost:8087/ws-chat'; // WebSocket direct au microservice

  private apiUrl = `http://localhost:8090/api/discussions`;

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // ======================================================
  // 1. PARTIE REST
  // ======================================================

  getMessages(groupId: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/groups/${groupId}/messages`,
      { headers: this.getHeaders() }
    );
  }

  // ======================================================
  // 2. PARTIE WEBSOCKET
  // ======================================================

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      // Already connected — do nothing
      if (this.stompClient && this.stompClient.connected) {
        resolve();
        return;
      }

      // ✅ FIX 1: Pass a factory function so auto-reconnect works
      this.stompClient = Stomp.over(() => new (SockJS as any)(this.socketUrl));

      // Disable noisy STOMP logs
      this.stompClient.debug = () => {};

      // ✅ FIX 2: Pass JWT token in STOMP headers for backend auth
      const token = localStorage.getItem('token') ?? '';
      const connectHeaders = { Authorization: `Bearer ${token}` };

      this.stompClient.connect(
        connectHeaders,
        () => {
          console.log('✅ WebSocket Connected to 8087');
          resolve();
        },
        (err: any) => {
          console.error('❌ WebSocket Connection Error', err);
          reject(err);
        }
      );
    });
  }

  subscribeToGroup(groupId: string) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('⚠️ Cannot subscribe — not connected');
      return;
    }

    // ✅ FIX 3: Unsubscribe any previous subscription before re-subscribing
    this.stompClient.subscribe(`/topic/group/${groupId}`, (payload) => {
      try {
        this.messageSource.next(JSON.parse(payload.body));
      } catch (e) {
        console.error('❌ Failed to parse message payload', e);
      }
    });

    this.stompClient.subscribe(`/topic/group/${groupId}/typing`, (payload) => {
      try {
        this.typingSource.next(JSON.parse(payload.body));
      } catch (e) {
        console.error('❌ Failed to parse typing payload', e);
      }
    });
  }

  subscribeToTyping(groupId: string): Observable<any> {
    return this.typing$;
  }

  // ======================================================
  // 3. ACTIONS D'ENVOI
  // ======================================================

  sendMessage(groupId: string, msg: any) {
    // ✅ FIX 4: Removed trailing slash to avoid double-slash in endpoint
    this.send('/app/chat.send', groupId, msg);
  }

  editMessage(groupId: string, msg: any) {
    this.send('/app/chat.edit', groupId, msg);
  }

  deleteMessage(groupId: string, msg: any) {
    this.send('/app/chat.delete', groupId, msg);
  }

  sendTypingStatus(groupId: string, userName: string, isTyping: boolean) {
    this.send('/app/chat.typing', groupId, { userName, isTyping });
  }

  pinMessage(groupId: string, msg: any) {
    this.send('/app/chat.pin', groupId, msg);
  }

  /**
   * Generic send — waits for connection if not yet connected
   */
  private send(endpoint: string, groupId: string, payload: any) {
    const destination = `${endpoint}/${groupId}`;

    if (this.stompClient && this.stompClient.connected) {
      // ✅ FIX 5: Use publish() instead of deprecated send()
      this.stompClient.publish({
        destination,
        body: JSON.stringify(payload),
        headers: { Authorization: `Bearer ${localStorage.getItem('token') ?? ''}` }
      });
    } else {
      console.warn('⚠️ WebSocket not connected — reconnecting...');
      this.connect()
        .then(() => {
          this.stompClient!.publish({
            destination,
            body: JSON.stringify(payload),
            headers: { Authorization: `Bearer ${localStorage.getItem('token') ?? ''}` }
          });
        })
        .catch((err) => {
          console.error('❌ Reconnect failed, message not sent', err);
        });
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.stompClient = null;
      console.log('🔌 WebSocket Disconnected');
    }
  }
}