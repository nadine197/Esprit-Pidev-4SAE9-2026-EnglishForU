import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service'; // Verify this path matches your project structure

@Component({
  selector: 'app-tutor-sidebar',
  templateUrl: './tutor-sidebar.html',
})
export class TutorSidebarComponent implements OnInit {
  
  // Navigation links specific to Tutors
  menuItems = [
    { label: 'Dashboard', link: '/tutor/dashboard' },
    { label: 'My Students', link: '/tutor/students' },
    { label: 'Course Content', link: '/tutor/courses' },
    { label: 'Live Sessions', link: '/tutor/sessions' },
    { label: 'Settings', link: '/tutor/settings' }
  ];

  userName: string = 'Tutor';

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get the name of the logged-in tutor to display in the sidebar
    const user = this.authService.getUser();
    if (user) {
      this.userName = user.name;
    }
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}