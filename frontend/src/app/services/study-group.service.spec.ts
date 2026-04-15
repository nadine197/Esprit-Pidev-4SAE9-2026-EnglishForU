import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { StudyGroupService } from './study-group.service';
import { StudyGroup } from '../features/study-groups/models/study-group';

describe('StudyGroupService', () => {
  let service: StudyGroupService;
  let httpMock: HttpTestingController;
  const api = 'http://localhost:8084/api/study-groups';

  const mockGroup: StudyGroup = {
    groupId: 1,
    name: 'Groupe A',
    level: 'BEGINNER',
    location: 'Tunis',
    maxCapacity: 20,
    status: 'ACTIVE',
    studentsIds: [],
    startdate: '2025-01-01',
    enddate: '2025-06-01',
    courseId: 1,
    tutorId: 'uuid-tutor-1'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [StudyGroupService]
    });
    service = TestBed.inject(StudyGroupService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ── Test 1 : Service créé ─────────────────────────────────
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ── Test 2 : GET tous les groupes ─────────────────────────
  it('should get all study groups', () => {
    service.getAll().subscribe(groups => {
      expect(groups.length).toBe(1);
      expect(groups[0].name).toBe('Groupe A');
    });

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('GET');
    req.flush([mockGroup]);
  });

  // ── Test 3 : GET groupe par ID ────────────────────────────
  it('should get study group by id', () => {
    service.getById(1).subscribe(group => {
      expect(group.groupId).toBe(1);
      expect(group.name).toBe('Groupe A');
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockGroup);
  });

  // ── Test 4 : POST créer un groupe ─────────────────────────
  it('should create a study group', () => {
    service.create(mockGroup).subscribe(group => {
      expect(group.name).toBe('Groupe A');
      expect(group.level).toBe('BEGINNER');
    });

    const req = httpMock.expectOne(api);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockGroup);
    req.flush(mockGroup);
  });

  // ── Test 5 : PUT modifier un groupe ──────────────────────
  it('should update a study group', () => {
    const updated = { ...mockGroup, name: 'Groupe Modifié' };

    service.update(1, updated).subscribe(group => {
      expect(group.name).toBe('Groupe Modifié');
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updated);
  });

  // ── Test 6 : DELETE supprimer un groupe ───────────────────
  it('should delete a study group', () => {
    service.delete(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${api}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  // ── Test 7 : GET stats ────────────────────────────────────
  it('should get stats', () => {
    const mockStats = {
      totalGroups: 5,
      activeGroups: 3,
      totalStudents: 20
    };

    service.getStats().subscribe(stats => {
      expect(stats.totalGroups).toBe(5);
      expect(stats.activeGroups).toBe(3);
    });

    const req = httpMock.expectOne(`${api}/stats`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStats);
  });

  // ── Test 8 : GET audit log ────────────────────────────────
  it('should get audit log', () => {
    const mockLog = [
      { revision: 1, type: 'INSERT', name: 'Groupe A' }
    ];

    service.getAuditLog(1).subscribe(log => {
      expect(log.length).toBe(1);
      expect(log[0].type).toBe('INSERT');
    });

    const req = httpMock.expectOne(`${api}/1/audit`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLog);
  });

  // ── Test 9 : POST chatbot ─────────────────────────────────
  it('should send chat message', () => {
    const mockReply = { reply: 'Bonjour ! Comment puis-je vous aider ?' };

    service.chat('bonjour').subscribe(res => {
      expect(res.reply).toBe('Bonjour ! Comment puis-je vous aider ?');
    });

    const req = httpMock.expectOne(
      r => r.url === `${api}/chatbot`
        && r.params.get('message') === 'bonjour'
    );
    expect(req.request.method).toBe('POST');
    req.flush(mockReply);
  });

  // ── Test 10 : POST chatbot avec groupId ───────────────────
  it('should send chat message with groupId', () => {
    const mockReply = { reply: 'Ce groupe est actif.' };

    service.chat('statut ?', 1).subscribe(res => {
      expect(res.reply).toBe('Ce groupe est actif.');
    });

    const req = httpMock.expectOne(
      r => r.url === `${api}/chatbot`
        && r.params.get('message') === 'statut ?'
        && r.params.get('groupId') === '1'
    );
    expect(req.request.method).toBe('POST');
    req.flush(mockReply);
  });

  // ── Test 11 : GET recherche ───────────────────────────────
  it('should search groups by name', () => {
    service.search({ name: 'English' }).subscribe(groups => {
      expect(groups.length).toBe(1);
      expect(groups[0].name).toBe('Groupe A');
    });

    const req = httpMock.expectOne(
      r => r.url === `${api}/search`
        && r.params.get('name') === 'English'
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockGroup]);
  });

  // ── Test 12 : GET calendrier par date ─────────────────────
  it('should get groups by date', () => {
    const date = new Date('2025-03-15');

    service.getByDate(date).subscribe(groups => {
      expect(groups.length).toBe(1);
    });

    const req = httpMock.expectOne(
      r => r.url === `${api}/calendar/by-date`
        && r.params.get('date') === '2025-03-15'
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockGroup]);
  });

  // ── Test 13 : GET calendrier par mois ────────────────────
  it('should get groups by month', () => {
    service.getByMonth(2025, 3).subscribe(groups => {
      expect(groups.length).toBe(1);
    });

    const req = httpMock.expectOne(
      r => r.url === `${api}/calendar/by-month`
        && r.params.get('year') === '2025'
        && r.params.get('month') === '3'
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockGroup]);
  });

  // ── Test 14 : formatDate ──────────────────────────────────
  it('should format date correctly', () => {
    const date = new Date(2025, 2, 5);
    const formatted = service.formatDate(date);
    expect(formatted).toBe('2025-03-05');
  });

  // ── Test 15 : getStatusLabel ACTIVE ──────────────────────
  it('should return Actif for ACTIVE status', () => {
    expect(service.getStatusLabel('ACTIVE')).toBe('Actif');
  });

  // ── Test 16 : getStatusLabel PLANNED ─────────────────────
  it('should return Planifié for PLANNED status', () => {
    expect(service.getStatusLabel('PLANNED')).toBe('Planifié');
  });

  // ── Test 17 : getStatusLabel COMPLETED ───────────────────
  it('should return Terminé for COMPLETED status', () => {
    expect(service.getStatusLabel('COMPLETED')).toBe('Terminé');
  });

  // ── Test 18 : getStatusLabel CANCELLED ───────────────────
  it('should return Annulé for CANCELLED status', () => {
    expect(service.getStatusLabel('CANCELLED')).toBe('Annulé');
  });

  // ── Test 19 : getStatusColor ──────────────────────────────
  it('should return correct status colors', () => {
    expect(service.getStatusColor('ACTIVE')).toBe('#1D9E75');
    expect(service.getStatusColor('PLANNED')).toBe('#185FA5');
    expect(service.getStatusColor('CANCELLED')).toBe('#E24B4A');
    expect(service.getStatusColor('COMPLETED')).toBe('#534AB7');
  });

  // ── Test 20 : getCapacityPercent 50% ─────────────────────
  it('should calculate capacity percent correctly', () => {
    const group: StudyGroup = {
      ...mockGroup,
      maxCapacity: 10,
      studentsIds: ['1', '2', '3', '4', '5']
    };
    expect(service.getCapacityPercent(group)).toBe(50);
  });

  // ── Test 21 : getCapacityPercent 0 ───────────────────────
  it('should return 0 when maxCapacity is 0', () => {
    const group: StudyGroup = {
      ...mockGroup,
      maxCapacity: 0,
      studentsIds: []
    };
    expect(service.getCapacityPercent(group)).toBe(0);
  });

  // ── Test 22 : getCapacityColor rouge ─────────────────────
  it('should return red when capacity >= 90%', () => {
    const group: StudyGroup = {
      ...mockGroup,
      maxCapacity: 10,
      studentsIds: ['1','2','3','4','5','6','7','8','9','10']
    };
    expect(service.getCapacityColor(group)).toBe('#E24B4A');
  });

  // ── Test 23 : getCapacityColor orange ────────────────────
  it('should return orange when capacity >= 60%', () => {
    const group: StudyGroup = {
      ...mockGroup,
      maxCapacity: 10,
      studentsIds: ['1','2','3','4','5','6','7']
    };
    expect(service.getCapacityColor(group)).toBe('#BA7517');
  });

  // ── Test 24 : getCapacityColor vert ──────────────────────
  it('should return green when capacity < 60%', () => {
    const group: StudyGroup = {
      ...mockGroup,
      maxCapacity: 10,
      studentsIds: ['1','2','3']
    };
    expect(service.getCapacityColor(group)).toBe('#1D9E75');
  });

  // ── Test 25 : getRevisionTypeLabel ───────────────────────
  it('should return correct revision type labels', () => {
    expect(service.getRevisionTypeLabel('INSERT')).toBe('Créé');
    expect(service.getRevisionTypeLabel('UPDATE')).toBe('Modifié');
    expect(service.getRevisionTypeLabel('DELETE')).toBe('Supprimé');
  });

  // ── Test 26 : getRevisionTypeColor ───────────────────────
  it('should return correct revision type colors', () => {
    expect(service.getRevisionTypeColor('INSERT')).toBe('#1D9E75');
    expect(service.getRevisionTypeColor('UPDATE')).toBe('#2563eb');
    expect(service.getRevisionTypeColor('DELETE')).toBe('#E24B4A');
  });
});
