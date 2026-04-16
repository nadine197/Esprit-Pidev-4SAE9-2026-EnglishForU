import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-student-layout',
  templateUrl: './student-layout.html'
})
export class StudentLayoutComponent implements OnInit {
  currentUser: any = null;

  navLinks = [
    { label: 'Home', path: '/student/home' },
    { label: 'Clubs', path: '/student/clubs' },
    { label: 'Events', path: '/student/events' },
    { label: 'My Tickets', path: '/student/my-tickets' }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
  }

  get displayName(): string {
    if (!this.currentUser) return '';
    const parts = [this.currentUser.name, this.currentUser.lastName].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : this.currentUser.email || 'Student';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

