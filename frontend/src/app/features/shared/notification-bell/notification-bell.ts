import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription, interval, startWith, switchMap } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { AppNotification, NotificationsService } from '../../../services/notifications.service';

@Component({
  selector: 'app-notification-bell',
  templateUrl: './notification-bell.html',
  styleUrls: ['./notification-bell.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: AppNotification[] = [];
  unreadCount = 0;
  dropdownOpen = false;
  isHelpDesk = false;

  toastMessage = '';
  private pollSub?: Subscription;
  private initialized = false;

  constructor(
    private authService: AuthService,
    private notificationsService: NotificationsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    this.isHelpDesk = user?.role === 'HELP_DESK';

    if (!this.isHelpDesk) {
      return;
    }

    this.pollSub = interval(30000)
      .pipe(
        startWith(0),
        switchMap(() => this.notificationsService.list())
      )
      .subscribe((notifications) => {
        const previousUnread = this.unreadCount;
        this.notifications = notifications;
        this.unreadCount = notifications.filter((notification) => !notification.read).length;

        if (this.initialized && this.unreadCount > previousUnread) {
          this.showToast('New report notification received');
        }

        this.initialized = true;
      });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  openNotification(notification: AppNotification): void {
    if (!notification.read) {
      this.notificationsService.markRead(notification.id).subscribe(() => {
        notification.read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      });
    }

    this.dropdownOpen = false;
    this.router.navigate(['/helpdesk/board'], {
      queryParams: notification.reportId ? { ticketId: notification.reportId } : {}
    });
  }

  markRead(notification: AppNotification, event: Event): void {
    event.stopPropagation();
    if (notification.read) {
      return;
    }

    this.notificationsService.markRead(notification.id).subscribe(() => {
      notification.read = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    });
  }

  private showToast(message: string): void {
    this.toastMessage = message;
    setTimeout(() => {
      this.toastMessage = '';
    }, 3000);
  }
}
