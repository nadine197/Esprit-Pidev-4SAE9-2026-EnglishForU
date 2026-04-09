import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import {
  StudyGroupNotificationService,
  GroupNotification
} from '../../../../services/study-group-notification.service';

@Component({
  selector: 'app-notification-bell',
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {

  notifications: GroupNotification[] = [];
  showPanel = false;
  private sub!: Subscription;

  get unreadCount(): number {
    return this.notifications.length;
  }

  constructor(public notifService: StudyGroupNotificationService) {}

  ngOnInit(): void {
    this.notifService.connect();
    this.sub = this.notifService.notifications$.subscribe(notif => {
      this.notifications.unshift(notif);
      if (this.notifications.length > 20) this.notifications.pop();
    });
  }

  togglePanel():  void { this.showPanel = !this.showPanel; }
  clearAll():     void { this.notifications = []; }

  formatTime(ts: string): string {
    try {
      return new Date(ts).toLocaleTimeString('fr-FR', {
        hour: '2-digit', minute: '2-digit'
      });
    } catch { return ts; }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.notifService.disconnect();
  }
}
