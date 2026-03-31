import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

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
      const loginData = {
        login: this.loginForm.value.email,
        password: this.loginForm.value.password,
        rememberMe: false
      };

      this.errorMessage = '';
      this.authService.login(loginData).subscribe({
        next: (res: any) => {
          this.loading = false;
          const role = res.user?.role;
          if (role === 'ADMIN') {
            this.router.navigate(['/admin/dashboard']);
          } else if (role === 'TUTOR') {
            this.router.navigate(['/tutor/dashboard']);
          } else {
            this.router.navigate(['/student/home']);
          }
        },
        error: (err: any) => {
          this.loading = false;
          if (err.status === 401) {
            this.errorMessage = "Invalid email or password. Please try again.";
          } else if (err.status === 403) {
            this.errorMessage = "Your account is not active. Please contact support.";
          } else if (err.status === 0) {
            this.errorMessage = "Unable to connect to server. Please check your connection.";
          } else {
            this.errorMessage = err.error?.message || "Login failed. Please try again.";
          }
        }
      });
    }
  }
}

