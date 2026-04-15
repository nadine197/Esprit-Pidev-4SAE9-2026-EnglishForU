import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CourseService } from './course.service';
import { Course } from '../features/courses/models/courses';

describe('CourseService', () => {
  let service: CourseService;
  let httpMock: HttpTestingController;
  const api = 'http://localhost:8084/api/courses';

  const mockCourse: Course = {
    courseid: 1,
    title: 'Java Spring Boot',
    description: 'Cours complet Spring Boot',
    duration: 40,
    adminId: 'uuid-admin-1'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CourseService]
    });
    service = TestBed.inject(CourseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Vérifie qu'aucune requête HTTP non attendue n'est en attente
  });

  // ── Test 1 : Service créé ─────────────────────────────────
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ── Test 2 : GET tous les cours ───────────────────────────
  it('should get all courses', () => {
    service.getAll().subscribe(courses => {
      expect(courses.length).toBe(1);
      expect(courses[0].title).toBe('Java Spring Boot');
    });

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('GET');
    req.flush([mockCourse]);
  });

  // ── Test 3 : GET liste vide ───────────────────────────────
  it('should return empty list when no courses', () => {
    service.getAll().subscribe(courses => {
      expect(courses.length).toBe(0);
    });

    const req = httpMock.expectOne(api);
    req.flush([]);
  });

  // ── Test 4 : GET cours par ID ─────────────────────────────
  it('should get course by id', () => {
    service.getById(1).subscribe(course => {
      expect(course.courseid).toBe(1);
      expect(course.title).toBe('Java Spring Boot');
      expect(course.duration).toBe(40);
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCourse);
  });

  // ── Test 5 : POST créer un cours ──────────────────────────
  it('should create a course', () => {
    const newCourse: Course = {
      courseid: 0,
      title: 'Angular Avancé',
      description: 'Maîtriser Angular',
      duration: 30,
      adminId: 'uuid-admin-2'
    };

    service.create(newCourse).subscribe(course => {
      expect(course).toBeTruthy();
    });

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newCourse);
    req.flush({ ...newCourse, courseid: 2 });
  });

  // ── Test 6 : PUT modifier un cours ────────────────────────
  it('should update a course', () => {
    const updated: Course = { ...mockCourse, title: 'Titre Modifié', duration: 50 };

    service.update(1, updated).subscribe(course => {
      expect(course).toBeTruthy();
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush(updated);
  });

  // ── Test 7 : DELETE supprimer un cours ────────────────────
  it('should delete a course', () => {
    service.delete(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ── Test 8 : GET tri par durée ────────────────────────────
  it('should get courses sorted by duration', () => {
    const sorted: Course[] = [
      { courseid: 1, title: 'Court',  description: '', duration: 10, adminId: 'a' },
      { courseid: 2, title: 'Moyen',  description: '', duration: 30, adminId: 'a' },
      { courseid: 3, title: 'Long',   description: '', duration: 60, adminId: 'a' }
    ];

    service.sortByDuration().subscribe(courses => {
      expect(courses.length).toBe(3);
      expect(courses[0].duration).toBeLessThanOrEqual(courses[1].duration);
      expect(courses[1].duration).toBeLessThanOrEqual(courses[2].duration);
    });

    const req = httpMock.expectOne(`${api}/sort/duration`);
    expect(req.request.method).toBe('GET');
    req.flush(sorted);
  });

  // ── Test 9 : GET recherche par mot-clé ────────────────────
  it('should search courses by keyword', () => {
    service.search('spring').subscribe(courses => {
      expect(courses.length).toBe(1);
      expect(courses[0].title).toContain('Spring');
    });

    const req = httpMock.expectOne(`${api}/search?keyword=spring`);
    expect(req.request.method).toBe('GET');
    req.flush([mockCourse]);
  });

  // ── Test 10 : GET recherche sans résultat ─────────────────
  it('should return empty list when search has no match', () => {
    service.search('xyz').subscribe(courses => {
      expect(courses.length).toBe(0);
    });

    const req = httpMock.expectOne(`${api}/search?keyword=xyz`);
    req.flush([]);
  });

  // ── Test 11 : GET export PDF → blob ──────────────────────
  it('should download PDF as blob', () => {
    const fakeBlob = new Blob(['PDF'], { type: 'application/pdf' });

    service.downloadPdf().subscribe(blob => {
      expect(blob).toBeTruthy();
      expect(blob instanceof Blob).toBeTrue();
    });

    const req = httpMock.expectOne(`${api}/pdf`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(fakeBlob);
  });

  // ── Test 12 : GET export Excel → blob ────────────────────
  it('should download Excel as blob', () => {
    const fakeBlob = new Blob(['EXCEL'], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    service.downloadExcel().subscribe(blob => {
      expect(blob).toBeTruthy();
      expect(blob instanceof Blob).toBeTrue();
    });

    const req = httpMock.expectOne(`${api}/excel`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(fakeBlob);
  });

  // ── Test 13 : Erreur HTTP 404 ─────────────────────────────
  it('should handle 404 error on getById', () => {
    service.getById(99).subscribe({
      next: () => fail('Expected an error'),
      error: err => {
        expect(err.status).toBe(404);
      }
    });

    const req = httpMock.expectOne(`${api}/99`);
    req.flush('Not Found', { status: 404, statusText: 'Not Found' });
  });

  // ── Test 14 : Erreur HTTP 500 ─────────────────────────────
  it('should handle 500 server error on getAll', () => {
    service.getAll().subscribe({
      next: () => fail('Expected an error'),
      error: err => {
        expect(err.status).toBe(500);
      }
    });

    const req = httpMock.expectOne(api);
    req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
  });

  // ── Test 15 : Corps de la requête POST ────────────────────
  it('should send correct body on create', () => {
    service.create(mockCourse).subscribe();

    const req = httpMock.expectOne(api);
    expect(req.request.body.title).toBe('Java Spring Boot');
    expect(req.request.body.duration).toBe(40);
    req.flush(mockCourse);
  });
});
