import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service'; // Adjust path
import { Router } from '@angular/router';

type RegisterRole = 'STUDENT' | 'TUTOR' | 'HELP_DESK';

@Component({
  selector: 'app-register',
  templateUrl: './register.html'
})
export class RegisterComponent {
  registerForm: FormGroup;
  selectedRole: RegisterRole = 'STUDENT';
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.pattern(/^\S+\s+\S+.*$/)]],
      email: ['', [Validators.required, Validators.email]],
      prefix: ['+216', [Validators.required, Validators.pattern(/^\+?[0-9]{1,5}$/)]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9\-\s]{6,20}$/)]],
      role: ['STUDENT', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      agreeTerms: [false, Validators.requiredTrue]
    });
  }

  setRole(role: RegisterRole): void {
    this.selectedRole = role;
    this.registerForm.patchValue({ role });
  }

  onSubmit(): void {
    this.errorMessage = '';

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.errorMessage = 'Please complete all required fields correctly.';
      return;
    }

    const fullName = (this.registerForm.value.fullName || '').trim();
    const password = this.registerForm.value.password;
    const confirmPassword = this.registerForm.value.confirmPassword;

    if (password !== confirmPassword) {
      this.errorMessage = 'Password and confirm password must match.';
      return;
    }

    const normalizedPhoneData = this.normalizePhoneAndPrefix(
      String(this.registerForm.value.phone || ''),
      String(this.registerForm.value.prefix || '')
    );

    if (!normalizedPhoneData) {
      this.errorMessage = 'Please enter a valid country prefix and phone number.';
      return;
    }

    const nameParts = this.splitName(fullName);
    if (!nameParts) {
      this.errorMessage = 'Please enter both first name and last name.';
      return;
    }

    this.loading = true;

    const payload = {
      email: this.registerForm.value.email,
      password,
      phone: normalizedPhoneData.phone,
      role: this.selectedRole,
      name: nameParts.name,
      lastName: nameParts.lastName,
      prefix: normalizedPhoneData.prefix
    };

    this.authService.signup(payload).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = this.mapSignupError(err);
        console.error('Signup failed', err);
      }
    });
  }

  private splitName(fullName: string): { name: string; lastName: string } | null {
    const parts = fullName.split(/\s+/).filter((part) => !!part);
    if (parts.length < 2) {
      return null;
    }

    return {
      name: parts[0],
      lastName: parts.slice(1).join(' ')
    };
  }

  private normalizePhoneAndPrefix(rawPhone: string, rawPrefix: string): { phone: string; prefix: string } | null {
    const prefixValue = rawPrefix.trim();
    const normalizedPrefix = prefixValue.startsWith('+') ? prefixValue : `+${prefixValue}`;
    const normalizedPhone = rawPhone.replace(/\D/g, '');

    if (!/^\+?[0-9]{1,5}$/.test(normalizedPrefix)) {
      return null;
    }

    if (normalizedPhone.length < 6 || normalizedPhone.length > 20) {
      return null;
    }

    return {
      phone: normalizedPhone,
      prefix: normalizedPrefix
    };
  }

  private mapSignupError(err: any): string {
    const backendMessage = err?.error?.message;
    if (typeof backendMessage === 'string' && backendMessage.trim()) {
      return backendMessage;
    }

    if (err?.status === 409) {
      return 'An account with this email or phone already exists.';
    }

    if (err?.status === 400) {
      return 'Please check your full name, password (minimum 8 characters), prefix, and phone number.';
    }

    return 'Registration failed. Please try again.';
  }
}