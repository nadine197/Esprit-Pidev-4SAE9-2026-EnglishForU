import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const BASE = 'http://localhost:8090/api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // --- login ---
  describe('login', () => {
    it('should store token and user in localStorage when response contains a token', () => {
      const response = {
        token: 'jwt-abc',
        user: { name: 'John', lastName: 'Doe', email: 'john@test.com', role: 'STUDENT' }
      };

      service.login({ email: 'john@test.com', password: 'secret' }).subscribe();

      const req = httpMock.expectOne(`${BASE}/login`);
      expect(req.request.method).toBe('POST');
      req.flush(response);

      expect(localStorage.getItem('token')).toBe('jwt-abc');
      expect(JSON.parse(localStorage.getItem('user')!).email).toBe('john@test.com');
    });

    it('should NOT store anything when response has no token', () => {
      service.login({ email: 'john@test.com', password: 'wrong' }).subscribe();

      const req = httpMock.expectOne(`${BASE}/login`);
      req.flush({ message: 'Invalid credentials' });

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });

    it('should POST the credentials to the login endpoint', () => {
      const credentials = { email: 'test@test.com', password: '123456' };

      service.login(credentials).subscribe();

      const req = httpMock.expectOne(`${BASE}/login`);
      expect(req.request.body).toEqual(credentials);
      req.flush({});
    });
  });

  // --- getUser ---
  describe('getUser', () => {
    it('should return the parsed user object from localStorage', () => {
      const user = { name: 'Jane', email: 'jane@test.com', role: 'TUTOR' };
      localStorage.setItem('user', JSON.stringify(user));

      expect(service.getUser()).toEqual(user);
    });

    it('should return null when localStorage has no user entry', () => {
      expect(service.getUser()).toBeNull();
    });
  });

  // --- logout ---
  describe('logout', () => {
    it('should remove both token and user from localStorage', () => {
      localStorage.setItem('token', 'some-token');
      localStorage.setItem('user', JSON.stringify({ name: 'Jane' }));

      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });

    it('should not throw when localStorage is already empty', () => {
      expect(() => service.logout()).not.toThrow();
    });
  });

  // --- isLoggedIn ---
  describe('isLoggedIn', () => {
    it('should return true when a token is present in localStorage', () => {
      localStorage.setItem('token', 'some-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when no token exists in localStorage', () => {
      expect(service.isLoggedIn()).toBeFalse();
    });

    it('should return false after logout clears the token', () => {
      localStorage.setItem('token', 'some-token');
      service.logout();
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  // --- signup ---
  describe('signup', () => {
    it('should POST user data to the register-client endpoint', () => {
      const userData = { name: 'New', email: 'new@test.com', password: '123456', role: 'STUDENT' };

      service.signup(userData).subscribe();

      const req = httpMock.expectOne(`${BASE}/register-client`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(userData);
      req.flush({ message: 'Account created' });
    });
  });
});
