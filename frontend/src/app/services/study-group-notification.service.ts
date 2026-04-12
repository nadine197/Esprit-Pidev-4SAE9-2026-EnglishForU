import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, IMessage } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { Subject, Observable } from 'rxjs';

export interface GroupNotification {
  groupId:       number;
  type:          string;
  message:       string;
  groupName?:    string;
  contentTitle?: string;
  contentType?:  string;
  sessionDate?:  string;
  sender?:       string;
  timestamp:     string;
}

@Injectable({ providedIn: 'root' })
export class StudyGroupNotificationService implements OnDestroy {

  private client!: Client;
  private notif$  = new Subject<GroupNotification>();
  private api     = 'http://localhost:8084/api/study-groups';

  notifications$: Observable<GroupNotification> = this.notif$.asObservable();

  readonly TYPE_CONFIG: Record<string, {
    label: string; color: string; icon: string;
  }> = {
    ALMOST_FULL:       { label: 'Presque plein',   color: '#BA7517', icon: '⚠️' },
    FULL:              { label: 'Complet',          color: '#E24B4A', icon: '🔴' },
    STARTING_TOMORROW: { label: 'Démarre demain',   color: '#185FA5', icon: '📅' },
    ENDED:             { label: 'Terminé',          color: '#534AB7', icon: '✅' },
    CANCELLED:         { label: 'Annulé',           color: '#E24B4A', icon: '❌' },
    NEW_MEMBER:        { label: 'Nouveau membre',   color: '#1D9E75', icon: '👤' },
    NEW_CONTENT:       { label: 'Nouveau contenu',  color: '#7F77DD', icon: '📚' },
    NEW_SESSION:       { label: 'Nouvelle session', color: '#185FA5', icon: '🗓️' },
    NEW_MESSAGE:       { label: 'Nouveau message',  color: '#1D9E75', icon: '💬' },
  };

  constructor(private http: HttpClient) {}

  connect(): void {
    this.client = new Client({
      webSocketFactory: () =>
        new SockJS('http://localhost:8084/ws-notifications'),
      reconnectDelay: 3000,
      onConnect: () => {
        console.log('✅ WebSocket connecté');   // ← vérifie dans F12
        this.subscribeGlobal();
      },
      onDisconnect: () => console.log('❌ WebSocket déconnecté'),
      onStompError: (frame) => console.error('STOMP error', frame)
    });
    this.client.activate();
  }

  private subscribeGlobal(): void {
    this.client.subscribe(
      '/topic/study-groups/notifications',
      (msg: IMessage) => {
        console.log('📩 Notification reçue:', msg.body);  // ← vérifie dans F12
        this.notif$.next(JSON.parse(msg.body));
      }
    );
  }

  subscribeToGroup(groupId: number): void {
    if (!this.client?.connected) return;

    this.client.subscribe(
      `/topic/study-groups/${groupId}/notifications`,
      (msg: IMessage) => this.notif$.next(JSON.parse(msg.body))
    );

    this.client.subscribe(
      `/topic/study-group/${groupId}/alerts`,
      (msg: IMessage) => this.notif$.next(JSON.parse(msg.body))
    );
  }

  // ── Envoi WebSocket STOMP ─────────────────────────────────

  sendNewContent(groupId: number, title: string, type: string): void {
    if (!this.client?.connected) return;
    this.client.publish({
      destination: `/app/study-group/${groupId}/content`,
      body: JSON.stringify({ title, type })
    });
  }

  sendNewSession(groupId: number, date: string): void {
    if (!this.client?.connected) return;
    this.client.publish({
      destination: `/app/study-group/${groupId}/session`,
      body: JSON.stringify({ date })
    });
  }

  sendNewMessage(groupId: number, sender: string): void {
    if (!this.client?.connected) return;
    this.client.publish({
      destination: `/app/study-group/${groupId}/message`,
      body: JSON.stringify({ sender })
    });
  }

  // ── Envoi via HTTP (backup) ───────────────────────────────

  alertContent(groupId: number, title: string, type: string) {
    return this.http.post(
      `${this.api}/${groupId}/alert/content`,
      { title, type }
    );
  }

  getConfig(type: string) {
    return this.TYPE_CONFIG[type]
      ?? { label: type, color: '#888780', icon: '🔔' };
  }

  disconnect(): void  { this.client?.deactivate(); }
  ngOnDestroy(): void { this.disconnect(); }

}
