import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html'
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  currentUser: any = null; // Ajouté pour corriger l'erreur

  // Ajouté pour corriger l'erreur navLinks
  navLinks = [
    { label: "Community", path: "/community/feed", fragment: '' },
    { label: "Courses", path: "/courses", fragment: '' },
    { label: "Pricing", path: "/", fragment: "pricing" },
    { label: "Testimonials", path: "/", fragment: "testimonials" },
    { label: "Contact", path: "/", fragment: "footer" },
  ];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.currentUser = this.authService.getUser();
    }
  }

  logout() {
    this.authService.logout();
    this.isLoggedIn = false;
    this.router.navigate(['/login']);
  }
}