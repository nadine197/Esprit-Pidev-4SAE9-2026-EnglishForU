import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html'
})
export class SidebarComponent {
  // Flag to toggle the dropdown in the UI
  isUserMenuOpen = false;

  menuItems = [
    { label: 'Dashboard', link: '/admin/dashboard', icon: 'grid' },
    { 
      label: 'User Management', 
      link: '/admin/users/all', 
      icon: 'users',
      hasSubmenu: true // Marks this as a dropdown
    },
    { label: 'Course Catalog', link: '/admin/courses', icon: 'book' },
      { label: 'Promo Codes', link: '/admin/promos', icon: 'tag' },
            { label: 'Payments Logs', link: '/admin/payments', icon: 'tag' },

  { label: 'Package Management', link: '/admin/packages', icon: 'box' },
{ label: 'Appointments', link: '/admin/appointments', icon: 'calendar' },
    { label: 'Discussion Groups', link: '/admin/discussions', icon: 'message-square' }, 
        { label: 'Settings', link: '/admin/settings', icon: 'settings' },
  ];

  // Specific sub-items for User Management
  userSubItems = [
    { label: 'Admin', link: '/admin/users/all' },
    { label: 'Students', link: '/admin/users/STUDENT' },
    { label: 'Tutors', link: '/admin/users/TUTOR' }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  toggleUserMenu() {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}