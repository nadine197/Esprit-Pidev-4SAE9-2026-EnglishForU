import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

interface NavLink {
  label: string;
  path: string;
  fragment: string;
  requiredRoles?: string[];
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html'
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  currentUser: any = null;
  private readonly threadAccessRoles = ['STUDENT', 'TUTOR', 'TEACHER', 'HELP_DESK'];

  private readonly baseNavLinks: NavLink[] = [
    { label: 'Courses', path: '/courses', fragment: '' },
    { label: 'Threads', path: '/community/feed', fragment: '', requiredRoles: this.threadAccessRoles },
    { label: 'Ticket Board', path: '/helpdesk/board', fragment: '', requiredRoles: ['HELP_DESK'] },
    { label: 'Pricing', path: '/', fragment: 'pricing' },
    { label: 'Testimonials', path: '/', fragment: 'testimonials' },
    { label: 'Contact', path: '/', fragment: 'footer' },
  ];
  navLinks: NavLink[] = [];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.refreshUserState();
  }

  private refreshUserState(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.currentUser = this.authService.getUser();
    } else {
      this.currentUser = null;
    }

    const currentRole = (this.currentUser?.role || '').toUpperCase();

    this.navLinks = this.baseNavLinks.filter((link) => {
      if (!link.requiredRoles || link.requiredRoles.length === 0) {
        return true;
      }

      return link.requiredRoles.includes(currentRole);
    });
  }

  logout() {
    this.authService.logout();
    this.refreshUserState();
    this.router.navigate(['/login']);
  }

  formatRole(role?: string): string {
    const normalized = (role || '').toUpperCase();
    if (normalized === 'HELP_DESK') {
      return 'Help Desk';
    }

    if (normalized === 'SUPER_ADMIN') {
      return 'Super Admin';
    }

    if (!normalized) {
      return 'User';
    }

    return normalized.charAt(0) + normalized.slice(1).toLowerCase();
  }

  canAccessThreads(): boolean {
    const role = (this.currentUser?.role || '').toUpperCase();
    return this.threadAccessRoles.includes(role);
  }

  getUserDisplayName(): string {
    const firstName = (this.currentUser?.name || '').toString().trim();
    const lastName = (this.currentUser?.lastName || '').toString().trim();
    const fullName = `${firstName} ${lastName}`.trim();
    return fullName || firstName || 'User';
  }

  getUserInitials(): string {
    const fullName = this.getUserDisplayName();
    const segments = fullName
      .split(/\s+/)
      .map((segment: string) => segment.trim())
      .filter((segment: string) => !!segment);

    if (segments.length === 0) {
      return 'US';
    }

    const first = segments[0].charAt(0).toUpperCase();
    const second = segments.length > 1
      ? segments[1].charAt(0).toUpperCase()
      : (segments[0].charAt(1) || '').toUpperCase();

    return `${first}${second}`.trim();
  }
}