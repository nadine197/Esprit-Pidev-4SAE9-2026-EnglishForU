import { inject } from '@angular/core';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '..//services/auth.service';

export const authGuard = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const user = authService.getUser();
  const expectedRoles = route.data['roles'] as Array<string>;

  if (expectedRoles && !expectedRoles.includes(user?.role)) {
    router.navigate(['/main']);
    return false;
  }

  return true;
};

