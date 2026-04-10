import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service'; // Cleaned path

@Component({
  selector: 'app-login',
  templateUrl: './login.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit() {
  if (this.loginForm.valid) {
    this.loading = true;
<<<<<<< HEAD
=======
    this.errorMessage = '';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
    const loginData = {
      login: this.loginForm.value.email,
      password: this.loginForm.value.password,
      rememberMe: false
    };

    this.authService.login(loginData).subscribe({
  next: (res: any) => {
    this.loading = false;
    
    // Now res.user.role will be "ADMIN" or "STUDENT"
    const role = res.user?.role;
    console.log("User Role received:", role);
<<<<<<< HEAD
=======
    if (role) {
      localStorage.setItem('ROLE', role);
    } else {
      localStorage.removeItem('ROLE');
    }
    if (res.user?.id) {
      localStorage.setItem('USER_ID', String(res.user.id));
    } else {
      localStorage.removeItem('USER_ID');
    }
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

    if (role === 'ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    
    } else if (role === 'TUTOR') {
      this.router.navigate(['/tutor/dashboard']);
    } else {
      this.router.navigate(['/student-home']); 
    }
  },
  error: (err: any) => {
    this.loading = false;
<<<<<<< HEAD
    this.errorMessage = "Login failed.";
=======
    if (err?.status === 0) {
      this.errorMessage = 'Cannot reach the backend. Start the Gateway on port 8090.';
    } else {
      this.errorMessage = err.error?.message || 'Login failed.';
    }
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  }
});
  }
}
<<<<<<< HEAD
}
=======
}
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
