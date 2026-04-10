import { Component, OnInit } from '@angular/core';
<<<<<<< HEAD
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
=======
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html'
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
<<<<<<< HEAD
  currentUser: any = null; // Ajouté pour corriger l'erreur

  // Ajouté pour corriger l'erreur navLinks
  navLinks = [
    { label: "Courses", path: "/courses", fragment: '' },
    { label: "Pricing", path: "/", fragment: "pricing" },
    { label: "Testimonials", path: "/", fragment: "testimonials" },
    { label: "Contact", path: "/", fragment: "footer" },
  ];

  constructor(private authService: AuthService, private router: Router) {}

=======
  currentUser: any = null;

  constructor(private authService: AuthService, private router: Router) {}

  get navLinks() {
    let quizPath = '/login';

    if (this.isLoggedIn) {
      if (this.currentUser?.role === 'ADMIN') {
        quizPath = '/admin/quizzes';
      } else if (this.currentUser?.role === 'STUDENT') {
        quizPath = '/student-quizzes';
      } else {
        // Fallback for other roles or generic path
        quizPath = '/courses'; 
      }
    }

    return [
      { label: 'Courses', path: '/courses', fragment: '' },
      { label: 'Quizzes', path: quizPath, fragment: '' },
      { label: 'Pricing', path: '/main', fragment: 'pricing' },
      { label: 'Testimonials', path: '/main', fragment: 'testimonials' },
      { label: 'Contact', path: '/main', fragment: 'footer' }
    ];
  }

>>>>>>> 21f8a6f (metier avancer + controle de saisie)
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
<<<<<<< HEAD
}
=======
}
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
