import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'JobBoard';

  constructor(private router: Router) {}

  ngOnInit() {}

  isUserConnected(): boolean {
    const user = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    
    // Si on a un utilisateur ET un token, on considère qu'il est connecté
    return user !== null && token !== null;
  }
}
