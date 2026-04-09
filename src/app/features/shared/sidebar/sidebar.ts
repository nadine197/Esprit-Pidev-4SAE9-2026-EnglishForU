import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html'
})
export class SidebarComponent {
  menuItems = [
    { label: 'Dashboard', link: '/admin/dashboard' },
    { label: 'User Management', link: '/admin/users' },
    { label: 'Students', link: '/admin/users' },
    { label: 'Courses', link:'/courses' },
    { label: 'Contents', link:'/contents' },
    { label: 'study-groups', link:'/study-groups' },
    { label: 'Settings', link: '/admin/settings' },
  ];

  constructor(private authService: AuthService, private router: Router) {}

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
