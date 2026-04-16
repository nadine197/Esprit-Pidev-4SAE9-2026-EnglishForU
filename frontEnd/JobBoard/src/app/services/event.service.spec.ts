import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EventService } from './event.service';

describe('EventService', () => {
  let service: EventService;
  let httpMock: HttpTestingController;
  const BASE = 'http://localhost:8090/api/events';

  // Helper: strip query string before comparing base path
  const urlBase = (req: any) => req.url.split('?')[0];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EventService]
    });
    service = TestBed.inject(EventService);
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

  // --- Authorization header ---
  describe('Authorization header', () => {
    it('should attach a Bearer token to every request', () => {
      setUser('John', 'Doe', 'john@test.com', 'my-jwt');

      service.getAllEvents().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE);
      expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt');
      req.flush([]);
    });
  });

  // --- userId query param ---
  describe('userId query param', () => {
    it('should append the logged-in user email as userId', () => {
      setUser('John', 'Doe', 'john@test.com');

      service.getAllEvents().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE);
      expect(req.request.url).toContain('userId=john@test.com');
      req.flush([]);
    });

    it('should use an empty userId when no user is stored', () => {
      service.getAllEvents().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE);
      expect(req.request.url).toContain('userId=');
      req.flush([]);
    });
  });

  // --- userName composition ---
  describe('userName composition', () => {
    it('should build userName from name + lastName in participate URL', () => {
      setUser('John', 'Doe', 'john@test.com');

      service.participate(1).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/1/participate`);
      expect(req.request.url).toContain('userName=John%20Doe');
      req.flush({});
    });

    it('should fall back to email when name and lastName are both empty', () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ name: '', lastName: '', email: 'john@test.com' }));

      service.participate(1).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/1/participate`);
      expect(req.request.url).toContain('userName=john%40test.com');
      req.flush({});
    });

    it('should use only the first name when lastName is missing', () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ name: 'Alice', email: 'alice@test.com' }));

      service.participate(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/participate`);
      expect(req.request.url).toContain('userName=Alice');
      req.flush({});
    });
  });

  // --- CRUD ---
  describe('CRUD operations', () => {
    beforeEach(() => setUser('Jane', 'Smith', 'jane@test.com'));

    it('getAllEvents should GET from base URL', () => {
      service.getAllEvents().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'GET');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('getEventsByClub should GET from the club-specific URL', () => {
      service.getEventsByClub(5).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/club/5`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('getEventById should GET from the event URL', () => {
      service.getEventById(3).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/3`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });

    it('createEvent should POST event data to base URL', () => {
      const event = { title: 'Spring Festival', description: 'A great event' };
      service.createEvent(event).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === BASE && r.method === 'POST');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(event);
      req.flush({ id: 42 });
    });

    it('updateEvent should PUT event data to the event URL', () => {
      const update = { title: 'Updated Title' };
      service.updateEvent(3, update).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/3` && r.method === 'PUT');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(update);
      req.flush({});
    });

    it('deleteEvent should DELETE the event URL', () => {
      service.deleteEvent(3).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/3` && r.method === 'DELETE');
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  // --- Participation management ---
  describe('Participation management', () => {
    beforeEach(() => setUser('Jane', 'Smith', 'jane@test.com'));

    it('participate should POST to the participate endpoint', () => {
      service.participate(10).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/10/participate`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });

    it('acceptParticipant should PUT to the accept endpoint', () => {
      service.acceptParticipant(10, 5).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/10/participants/5/accept`);
      expect(req.request.method).toBe('PUT');
      req.flush({});
    });

    it('rejectParticipant should PUT to the reject endpoint', () => {
      service.rejectParticipant(10, 5).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/10/participants/5/reject`);
      expect(req.request.method).toBe('PUT');
      req.flush({});
    });

    it('getParticipants should GET the full participants list', () => {
      service.getParticipants(10).subscribe();

      const req = httpMock.expectOne(`${BASE}/10/participants`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('getPendingParticipants should GET only pending participants', () => {
      service.getPendingParticipants(10).subscribe();

      const req = httpMock.expectOne(`${BASE}/10/participants/pending`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  // --- Payment ---
  describe('Payment operations', () => {
    beforeEach(() => setUser('Jane', 'Smith', 'jane@test.com'));

    it('createPaymentIntent should POST with the given userEmail', () => {
      service.createPaymentIntent(7, 'jane@test.com').subscribe();

      const req = httpMock.expectOne(`${BASE}/7/payment/intent`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ userEmail: 'jane@test.com' });
      req.flush({ clientSecret: 'secret_123' });
    });

    it('confirmPayment should POST the full body to the confirm endpoint', () => {
      const body = {
        paymentIntentId: 'pi_abc',
        latitude: 36.8,
        longitude: 10.1,
        userName: 'Jane Smith',
        userEmail: 'jane@test.com'
      };
      service.confirmPayment(7, body).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/7/payment/confirm`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush({ status: 'confirmed' });
    });

    it('getMyPurchases should GET the purchases endpoint for current user', () => {
      service.getMyPurchases().subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/my-purchases`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  // --- Pass / Receipt ---
  describe('Pass and receipt downloads', () => {
    beforeEach(() => setUser('Jane', 'Smith', 'jane@test.com'));

    it('downloadPass should GET the pass with view=false', () => {
      service.downloadPass(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/pass` && r.url.includes('view=false'));
      expect(req.request.method).toBe('GET');
      req.flush(new Blob());
    });

    it('openPass should GET the pass with view=true', () => {
      service.openPass(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/pass` && r.url.includes('view=true'));
      expect(req.request.url).toContain('view=true');
      req.flush(new Blob());
    });

    it('downloadReceipt should GET the receipt with view=false', () => {
      service.downloadReceipt(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/receipt` && r.url.includes('view=false'));
      expect(req.request.url).toContain('view=false');
      req.flush(new Blob());
    });

    it('openReceipt should GET the receipt with view=true', () => {
      service.openReceipt(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/receipt` && r.url.includes('view=true'));
      expect(req.request.url).toContain('view=true');
      req.flush(new Blob());
    });

    it('getPassUrl should GET the pass URL object', () => {
      service.getPassUrl(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/pass-url`);
      expect(req.request.method).toBe('GET');
      req.flush({ url: 'https://example.com/pass.pdf' });
    });

    it('getReceiptUrl should GET the receipt URL object', () => {
      service.getReceiptUrl(2).subscribe();

      const req = httpMock.expectOne(r => urlBase(r) === `${BASE}/2/receipt-url`);
      expect(req.request.method).toBe('GET');
      req.flush({ url: 'https://example.com/receipt.pdf' });
    });
  });
});
