import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ContentService } from './content.service';
import { Content } from '../features/contents/models/content';

describe('ContentService', () => {
  let service: ContentService;
  let httpMock: HttpTestingController;
  const api = 'http://localhost:8084/api/contents';

  const mockContent: Content = {
    contentId: 1,
    title: 'Introduction à Java',
    type: 'VIDEO',
    url: 'https://example.com/intro-java',
    courseId: 1,
    authorId: 'uuid-author-1'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ContentService]
    });
    service = TestBed.inject(ContentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ── Test 1 : Service créé ─────────────────────────────────
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ── Test 2 : GET tous les contenus ────────────────────────
  it('should get all contents', () => {
    service.getAll().subscribe(contents => {
      expect(contents.length).toBe(1);
      expect(contents[0].title).toBe('Introduction à Java');
      expect(contents[0].type).toBe('VIDEO');
    });

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('GET');
    req.flush([mockContent]);
  });

  // ── Test 3 : GET liste vide ───────────────────────────────
  it('should return empty list when no contents', () => {
    service.getAll().subscribe(contents => {
      expect(contents.length).toBe(0);
    });

    const req = httpMock.expectOne(api);
    req.flush([]);
  });

  // ── Test 4 : GET contenu par ID ───────────────────────────
  it('should get content by id', () => {
    service.getById(1).subscribe(content => {
      expect(content.contentId).toBe(1);
      expect(content.title).toBe('Introduction à Java');
      expect(content.type).toBe('VIDEO');
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockContent);
  });

  // ── Test 5 : POST créer un contenu ────────────────────────
  it('should create a content', () => {
    const newContent: Content = {
      contentId: 0,
      title: 'Spring Security PDF',
      type: 'PDF',
      url: 'https://example.com/spring-security.pdf',
      courseId: 2,
      authorId: 'uuid-author-2'
    };

    service.create(newContent).subscribe(content => {
      expect(content).toBeTruthy();
      expect(content.title).toBe('Spring Security PDF');
    });

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newContent);
    req.flush({ ...newContent, contentId: 2 });
  });

  // ── Test 6 : PUT modifier un contenu ─────────────────────
  it('should update a content', () => {
    const updated: Content = { ...mockContent, title: 'Titre Modifié', type: 'ARTICLE' };

    service.update(1, updated).subscribe(content => {
      expect(content.title).toBe('Titre Modifié');
      expect(content.type).toBe('ARTICLE');
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updated);
    req.flush(updated);
  });

  // ── Test 7 : DELETE supprimer un contenu ──────────────────
  it('should delete a content', () => {
    service.delete(1).subscribe({
      complete: () => expect(true).toBeTrue()
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ── Test 8 : GET recherche par mot-clé ────────────────────
  it('should search contents by keyword', () => {
    service.search('java').subscribe(contents => {
      expect(contents.length).toBe(1);
      expect(contents[0].title).toContain('Java');
    });

    const req = httpMock.expectOne(`${api}/search?keyword=java`);
    expect(req.request.method).toBe('GET');
    req.flush([mockContent]);
  });

  // ── Test 9 : GET recherche sans résultat ──────────────────
  it('should return empty list when search has no match', () => {
    service.search('xyz').subscribe(contents => {
      expect(contents.length).toBe(0);
    });

    const req = httpMock.expectOne(`${api}/search?keyword=xyz`);
    req.flush([]);
  });

  // ── Test 10 : GET export PDF → blob ──────────────────────
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

  // ── Test 11 : GET stats par type ─────────────────────────
  it('should get stats by type', () => {
    const mockStats = [
      ['VIDEO',   5],
      ['PDF',     3],
      ['ARTICLE', 2]
    ];

    service.getStatsByType().subscribe(stats => {
      expect(stats.length).toBe(3);
      expect(stats[0][0]).toBe('VIDEO');
      expect(stats[0][1]).toBe(5);
    });

    const req = httpMock.expectOne(`${api}/stats/type`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStats);
  });

  // ── Test 12 : GET stats par type liste vide ───────────────
  it('should return empty stats when no contents', () => {
    service.getStatsByType().subscribe(stats => {
      expect(stats.length).toBe(0);
    });

    const req = httpMock.expectOne(`${api}/stats/type`);
    req.flush([]);
  });

  // ── Test 13 : GET télécharger historique TXT → blob ───────
  it('should download history as txt blob', () => {
    const fakeBlob = new Blob(['CREATE Content - 2025-01-01'], {
      type: 'text/plain'
    });

    service.downloadHistory().subscribe(blob => {
      expect(blob).toBeTruthy();
      expect(blob instanceof Blob).toBeTrue();
    });

    const req = httpMock.expectOne(`${api}/history/txt`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(fakeBlob);
  });

  // ── Test 14 : Erreur HTTP 404 sur getById ─────────────────
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

  // ── Test 15 : Erreur HTTP 500 sur getAll ──────────────────
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

  // ── Test 16 : Corps de la requête POST ────────────────────
  it('should send correct body on create', () => {
    service.create(mockContent).subscribe();

    const req = httpMock.expectOne(api);
    expect(req.request.body.title).toBe('Introduction à Java');
    expect(req.request.body.type).toBe('VIDEO');
    expect(req.request.body.courseId).toBe(1);
    req.flush(mockContent);
  });

  // ── Test 17 : Corps de la requête PUT ─────────────────────
  it('should send correct body on update', () => {
    const updated: Content = { ...mockContent, title: 'Nouveau Titre' };
    service.update(1, updated).subscribe();

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.body.title).toBe('Nouveau Titre');
    req.flush(updated);
  });

  // ── Test 18 : Plusieurs types de contenu ─────────────────
  it('should handle multiple content types', () => {
    const contents: Content[] = [
      { ...mockContent, contentId: 1, type: 'VIDEO'   },
      { ...mockContent, contentId: 2, type: 'PDF'     },
      { ...mockContent, contentId: 3, type: 'ARTICLE' }
    ];

    service.getAll().subscribe(result => {
      expect(result.length).toBe(3);
      const types = result.map(c => c.type);
      expect(types).toContain('VIDEO');
      expect(types).toContain('PDF');
      expect(types).toContain('ARTICLE');
    });

    const req = httpMock.expectOne(api);
    req.flush(contents);
  });
});
