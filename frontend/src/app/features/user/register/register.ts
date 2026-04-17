import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service'; // Adjust path
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.html'
})
export class RegisterComponent {
  registerForm: FormGroup;
  selectedRole: string = 'student';
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      role: ['student'],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      agreeTerms: [false, Validators.requiredTrue]
    });
  }

  setRole(role: string) {
    this.selectedRole = role;
    this.registerForm.patchValue({ role: role });
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      this.authService.signup(this.registerForm.value).subscribe({
        // Add ': any' to fix the parameter errors
        next: (response: any) => {
          console.log('User registered successfully!', response);
          alert('Registration successful! Redirecting to login...');
          this.router.navigate(['/main']);
        },
        error: (err: any) => {
          this.loading = false;
          // Improved error handling
          this.errorMessage = err.error?.message || "Registration failed. Please try again.";
          console.error('Signup failed', err);
        }
      });
    }
  }
  }