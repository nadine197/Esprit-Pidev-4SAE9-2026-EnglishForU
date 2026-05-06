import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
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
    this.errorMessage = '';
    const email = (this.loginForm.value.email || '').trim().toLowerCase();
    const loginData = {
      login: email,
      password: this.loginForm.value.password,
      rememberMe: false
    };

    this.authService.login(loginData).subscribe({
  next: (res: any) => {
    this.loading = false;
    
    // Now res.user.role will be "ADMIN" or "STUDENT"
    const role = res.user?.role;
    console.log("User Role received:", role);

    if (role === 'ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    
    } else if (role === 'TUTOR') {
      this.router.navigate(['/tutor/dashboard']);
    } else {
      this.router.navigate(['/student-home']); 
    }
  },
  error: (err: HttpErrorResponse) => {
    this.loading = false;
    const backendMessage =
      typeof err.error?.message === 'string'
        ? err.error.message
        : typeof err.error === 'string'
          ? err.error
          : '';

    if (backendMessage.includes('INCORRECT_CREDENTIALS')) {
      this.errorMessage = 'Incorrect email or password.';
    } else if (backendMessage.includes('USER_NOT_FOUND')) {
      this.errorMessage = 'No account found with this email.';
    } else if (backendMessage.includes('USER_BLOCKED')) {
      this.errorMessage = 'Your account is blocked. Please contact support.';
    } else {
      this.errorMessage = 'Login failed. Please try again.';
    }
  }
});
  }
}
}
