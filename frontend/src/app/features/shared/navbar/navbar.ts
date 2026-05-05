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
  get quizPath() {
    return '/student-quizzes';
  }

  get evaluationPath() {
    return '/student-evaluations';
  }

  navLinks = [
    { label: "Courses", path: "/courses", fragment: '' },
    { label: "Pricing", path: "/", fragment: "pricing" },
    { label: "Quizzes", path: null, dynamic: 'quiz' },
    { label: "Evaluations", path: null, dynamic: 'evaluation' },
    { label: "Contact", path: "/", fragment: "footer" },
    { label: "Book a Test", path: "/book-test" },
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