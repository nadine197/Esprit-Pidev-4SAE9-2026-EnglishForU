import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
<<<<<<< HEAD
import { AuthService } from '../../../services/auth.service'; // Adjust path
import { Router } from '@angular/router';
=======
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

@Component({
  selector: 'app-register',
  templateUrl: './register.html'
})
export class RegisterComponent {
  registerForm: FormGroup;
<<<<<<< HEAD
  selectedRole: string = 'student';
=======
  selectedRole = 'STUDENT';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  loading = false;
  errorMessage = '';

  constructor(
<<<<<<< HEAD
    private fb: FormBuilder, 
=======
    private fb: FormBuilder,
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
<<<<<<< HEAD
      role: ['student'],
      password: ['', [Validators.required, Validators.minLength(6)]]
=======
      role: ['STUDENT', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      agreeTerms: [false, Validators.requiredTrue]
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
    });
  }

  setRole(role: string) {
<<<<<<< HEAD
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
=======
    this.selectedRole = role.toUpperCase();
    this.registerForm.patchValue({ role: this.selectedRole });
  }

  onSubmit() {
    if (!this.registerForm.valid) {
      return;
    }

    const { fullName, confirmPassword, agreeTerms, ...formValue } = this.registerForm.value;
    if (formValue.password !== confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const [name = '', ...rest] = String(fullName).trim().split(/\s+/);
    const lastName = rest.join(' ');

    this.loading = true;
    this.errorMessage = '';

    const signupPayload = {
      ...formValue,
      name,
      lastName,
      prefix: ''
    };

    this.authService.signup(signupPayload).subscribe({
      next: (response: any) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        if (response.user?.role) {
          localStorage.setItem('ROLE', response.user.role);
          localStorage.setItem('userRole', response.user.role);
        } else {
          localStorage.removeItem('ROLE');
          localStorage.removeItem('userRole');
        }
        if (response.user?.id) {
          localStorage.setItem('USER_ID', String(response.user.id));
        } else {
          localStorage.removeItem('USER_ID');
        }
        this.router.navigate(['/main']);
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        console.error('Signup failed', err);
      }
    });
  }
}
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
