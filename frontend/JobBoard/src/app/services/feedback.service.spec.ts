import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FeedbackService } from './feedback.service';

describe('FeedbackService', () => {
  let service: FeedbackService;
  let httpMock: HttpTestingController;
  const BASE = 'http://localhost:8090/api/feedback';

  // Helper: strip query string before comparing base path
  const urlBase = (req: any) => req.url.split('?')[0];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FeedbackService]
    });
    service = TestBed.inject(FeedbackService);
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

  // --- Fetch feedback ---
  describe('getEventFeedback', () => {
    it('should GET event feedback with current userId', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.getEventFeedback(5).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/event/5`);
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('userId=alice@test.com');
      req.flush({ reviews: [], myReview: null, canReview: false });
    });
  });

  describe('getClubFeedback', () => {
    it('should GET club feedback with current userId', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.getClubFeedback(3).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/club/3`);
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('userId=alice@test.com');
      req.flush({ reviews: [], myReview: null, canReview: true });
    });
  });

  // --- Stats ---
  describe('getEventStats', () => {
    it('should GET event stats from the stats endpoint', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.getEventStats(5).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/event/5/stats`);
      expect(req.request.method).toBe('GET');
      req.flush({ averageRating: 4.2, totalReviews: 10 });
    });
  });

  describe('getClubStats', () => {
    it('should GET club stats from the stats endpoint', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.getClubStats(3).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/club/3/stats`);
      expect(req.request.method).toBe('GET');
      req.flush({ averageRating: 3.8, totalReviews: 5 });
    });
  });

  // --- Submit feedback ---
  describe('submitEventFeedback', () => {
    it('should POST the correct payload with targetType EVENT', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.submitEventFeedback(5, 4, 'Great event!').subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'POST');
      expect(req.request.body).toEqual({
        targetType: 'EVENT',
        targetId: 5,
        rating: 4,
        comment: 'Great event!'
      });
      expect(req.request.url).toContain('userId=alice@test.com');
      req.flush({ id: 1 });
    });

    it('should encode the userName in the URL', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.submitEventFeedback(5, 5, 'Loved it').subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'POST');
      expect(req.request.url).toContain('userName=Alice%20Dev');
      req.flush({ id: 2 });
    });

    it('should accept a rating of 1 (minimum valid)', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.submitEventFeedback(5, 1, '').subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'POST');
      expect(req.request.body.rating).toBe(1);
      req.flush({ id: 3 });
    });
  });

  describe('submitClubFeedback', () => {
    it('should POST the correct payload with targetType CLUB', () => {
      setUser('Bob', 'Smith', 'bob@test.com');

      service.submitClubFeedback(3, 3, 'Decent club').subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'POST');
      expect(req.request.body).toEqual({
        targetType: 'CLUB',
        targetId: 3,
        rating: 3,
        comment: 'Decent club'
      });
      req.flush({ id: 4 });
    });
  });

  // --- Delete feedback ---
  describe('deleteFeedback', () => {
    it('should DELETE the feedback with the given id and current userId', () => {
      setUser('Alice', 'Dev', 'alice@test.com');

      service.deleteFeedback(7).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/7` && r.method === 'DELETE');
      expect(req.request.method).toBe('DELETE');
      expect(req.request.url).toContain('userId=alice@test.com');
      req.flush({});
    });
  });
});
