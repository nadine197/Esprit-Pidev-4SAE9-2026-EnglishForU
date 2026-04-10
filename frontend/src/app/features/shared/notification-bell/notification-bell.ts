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
  isLoading = false;

  toastMessage = '';
  private pollSub?: Subscription;
  private initialized = false;

  constructor(
    private authService: AuthService,
    private notificationsService: NotificationsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      return;
    }

    this.isLoading = true;

    this.pollSub = interval(30000)
      .pipe(
        startWith(0),
        switchMap(() => this.notificationsService.list())
      )
      .subscribe((notifications) => {
        this.isLoading = false;
        const previousUnread = this.unreadCount;
        this.notifications = notifications;
        this.unreadCount = notifications.filter((notification) => !notification.read).length;

        if (this.initialized && this.unreadCount > previousUnread) {
          this.showToast('You have new notifications.');
        }

        this.initialized = true;
      }, () => {
        this.isLoading = false;
        if (!this.initialized) {
          this.notifications = [];
          this.unreadCount = 0;
        }
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
    const link = (notification.link || '').trim();

    if (link) {
      this.router.navigateByUrl(link).catch(() => {
        this.router.navigate(['/main']);
      });
      return;
    }

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
