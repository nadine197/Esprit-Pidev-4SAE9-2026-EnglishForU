import { inject } from '@angular/core';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '..//services/auth.service';

export const authGuard = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Check if user is logged in
  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  // 2. Check Role-Based Access (if the route has a "roles" data requirement)
  const user = authService.getUser();
  const expectedRoles = route.data['roles'] as Array<string>;

  if (expectedRoles && !expectedRoles.includes(user?.role)) {
    // If user is a STUDENT but tries to go to /admin
    console.warn('Access denied: Unauthorized role');
    router.navigate(['/main']); // Redirect to student home
    return false;
  }

  return true;
};