import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pw = group.get('password')?.value;
  const cpw = group.get('confirmPassword')?.value;
  return pw === cpw ? null : { passwordMismatch: true };
}

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
    }, { validators: passwordsMatch });
  }

  setRole(role: string) {
    this.selectedRole = role;
    this.registerForm.patchValue({ role: role });
  }

  onSubmit() {
    this.registerForm.markAllAsTouched();
    if (this.registerForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      this.authService.signup(this.registerForm.value).subscribe({
        next: (response: any) => {
          alert('Registration successful! Redirecting to login...');
          this.router.navigate(['/main']);
        },
        error: (err: any) => {
          this.loading = false;
          const msg = err.error?.message || '';
          if (msg === 'EMAIL_ALREADY_IN_USE') {
            this.errorMessage = 'This email address is already registered.';
          } else if (msg === 'PHONE_ALREADY_IN_USE') {
            this.errorMessage = 'This phone number is already linked to another account.';
          } else {
            this.errorMessage = msg || 'Registration failed. Please try again.';
          }
        }
      });
    }
  }
}

