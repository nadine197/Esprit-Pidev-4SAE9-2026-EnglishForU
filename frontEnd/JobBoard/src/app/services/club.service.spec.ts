import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClubService } from './club.service';

describe('ClubService', () => {
  let service: ClubService;
  let httpMock: HttpTestingController;
  const BASE = 'http://localhost:8090/api/clubs';

  // Helper: strip query string before comparing base path
  const urlBase = (req: any) => req.url.split('?')[0];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClubService]
    });
    service = TestBed.inject(ClubService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  function setUser(name: string, lastName: string, email: string, token = 'test-token') {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ name, lastName, email }));
  }

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // --- Authorization ---
  describe('Authorization header', () => {
    it('should attach Bearer token to every request', () => {
      setUser('Alice', 'Martin', 'alice@test.com', 'bearer-xyz');

      service.getAllClubs().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE);
      expect(req.request.headers.get('Authorization')).toBe('Bearer bearer-xyz');
      req.flush([]);
    });
  });

  // --- userId query param ---
  describe('userId query param', () => {
    it('should append logged-in user email as userId', () => {
      setUser('Alice', 'Martin', 'alice@test.com');

      service.getAllClubs().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE);
      expect(req.request.url).toContain('userId=alice@test.com');
      req.flush([]);
    });

    it('should use an empty userId when no user is stored', () => {
      service.getAllClubs().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE);
      expect(req.request.url).toContain('userId=');
      req.flush([]);
    });
  });

  // --- CRUD ---
  describe('CRUD operations', () => {
    beforeEach(() => setUser('Alice', 'Martin', 'alice@test.com'));

    it('getAllClubs should GET from base URL', () => {
      service.getAllClubs().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'GET');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('getClubById should GET from the club-specific URL', () => {
      service.getClubById(7).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/7`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });

    it('getMyClubs should GET the /my endpoint', () => {
      service.getMyClubs().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/my`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('createClub should POST with club data, userId and userName', () => {
      const club = { name: 'Robotics Club', description: 'We build robots' };
      service.createClub(club).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'POST');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(club);
      expect(req.request.url).toContain('userId=alice@test.com');
      expect(req.request.url).toContain('userName=Alice%20Martin');
      req.flush({ id: 1 });
    });

    it('updateClub should PUT club data to the club URL', () => {
      const update = { name: 'Robotics Club v2' };
      service.updateClub(7, update).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/7` && r.method === 'PUT');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(update);
      req.flush({});
    });

    it('deleteClub should DELETE the club URL', () => {
      service.deleteClub(7).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/7` && r.method === 'DELETE');
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  // --- Membership management ---
  describe('Membership management', () => {
    beforeEach(() => setUser('Bob', 'Taylor', 'bob@test.com'));

    it('joinClub should POST with userId and userName', () => {
      service.joinClub(3).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/3/join`);
      expect(req.request.method).toBe('POST');
      expect(req.request.url).toContain('userId=bob@test.com');
      expect(req.request.url).toContain('userName=Bob%20Taylor');
      req.flush({});
    });

    it('acceptMember should PUT to the accept endpoint', () => {
      service.acceptMember(3, 99).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/3/members/99/accept`);
      expect(req.request.method).toBe('PUT');
      req.flush({});
    });

    it('rejectMember should PUT to the reject endpoint', () => {
      service.rejectMember(3, 99).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/3/members/99/reject`);
      expect(req.request.method).toBe('PUT');
      req.flush({});
    });

    it('getMembers should GET the full members list', () => {
      service.getMembers(3).subscribe();

      const req = httpMock.expectOne(`${BASE}/3/members`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('getPendingMembers should GET only pending members', () => {
      service.getPendingMembers(3).subscribe();

      const req = httpMock.expectOne(`${BASE}/3/members/pending`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });
});
