import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-tutor-layout',
  templateUrl: './tutor-layout.html'
})
export class TutorLayoutComponent implements OnInit {
  currentUser: any;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

