import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('authGuard', () => {
  let authService: AuthService;
  let router: Router;

  function buildRoute(roles?: string[]): ActivatedRouteSnapshot {
    const route = new ActivatedRouteSnapshot();
    (route as any).data = roles ? { roles } : {};
    return route;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        {
          provide: Router,
          useValue: { navigate: jasmine.createSpy('navigate') }
        }
      ]
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  // --- Not logged in ---
  it('should redirect to /login and return false when user is not logged in', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute()));

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  // --- Logged in, no role restriction ---
  it('should return true when user is logged in and no roles are required', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'getUser').and.returnValue({ role: 'STUDENT' });

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute()));

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  // --- Logged in, correct role ---
  it('should return true when user role matches required roles', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'getUser').and.returnValue({ role: 'STUDENT' });

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute(['STUDENT', 'TUTOR'])));

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  // --- Logged in, wrong role ---
  it('should redirect to /main and return false when user role is not in required roles', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'getUser').and.returnValue({ role: 'STUDENT' });

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute(['ADMIN'])));

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/main']);
  });

  // --- Logged in, TUTOR role accessing TUTOR-only route ---
  it('should return true for TUTOR accessing TUTOR-only route', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'getUser').and.returnValue({ role: 'TUTOR' });

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute(['TUTOR'])));

    expect(result).toBeTrue();
  });

  // --- Logged in, ADMIN role accessing ADMIN-only route ---
  it('should return true for ADMIN accessing ADMIN-only route', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'getUser').and.returnValue({ role: 'ADMIN' });

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute(['ADMIN'])));

    expect(result).toBeTrue();
  });

  // --- Edge case: user has no role ---
  it('should redirect to /main when user has no role and a role restriction exists', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'getUser').and.returnValue({ name: 'Anonymous' });

    const result = TestBed.runInInjectionContext(() => authGuard(buildRoute(['STUDENT'])));

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/main']);
  });
});
