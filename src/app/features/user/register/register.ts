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
  selectedRole: string = 'STUDENT';
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
      role: ['STUDENT', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      //  zedna hedhom khater la validation to93ed ghalta tant que mehomch mentionné fel
      //  .ts w enty aatehom esm tcontroli fehom bih fel html
      confirmPassword: ['', [Validators.required]],       
      agreeTerms: [false, [Validators.requiredTrue]]
    });
  }

  setRole(role: string) {
    this.selectedRole = role.toUpperCase();
    this.registerForm.patchValue({ role: this.selectedRole });
  }
onSubmit() {
  if (this.registerForm.valid) {
    this.loading = true;
    this.errorMessage = '';
    
    console.log('Role sent to backend:', this.registerForm.value.role); // <-- vérification

    this.authService.signup(this.registerForm.value).subscribe({
      next: (response: any) => {
        console.log('User registered successfully!', response);
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        this.router.navigate(['/main']);
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = err.error?.message || "Registration failed. Please try again.";
        console.error('Signup failed', err);
      }
    });
  }
}
  }