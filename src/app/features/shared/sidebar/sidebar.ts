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
<<<<<<< HEAD
    { label: 'User Management', link: '/admin/users' },
    { label: 'Students', link: '/admin/users' },
    { label: 'Courses', link:'/courses' },
    { label: 'Contents', link:'/contents' },
    { label: 'study-groups', link:'/study-groups' },
    { label: 'Settings', link: '/admin/settings' },
=======
    { label: 'User Management', link: '/admin/users' }, // Changed this
    { label: 'Students', link: '/admin/users' },
    { label: 'Quizzes', link: '/admin/quizzes' },
    { label: 'Courses', link: '/admin/courses' },
    { label: 'Settings', link: '/admin/settings' },

>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  ];

  constructor(private authService: AuthService, private router: Router) {}

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
