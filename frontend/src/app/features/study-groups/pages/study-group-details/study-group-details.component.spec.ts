import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StudyGroupDetailsComponent } from './study-group-details.component';
import { StudyGroupService } from '../../../../services/study-group.service';
import { StudyGroupNotificationService } from '../../../../services/study-group-notification.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { StudyGroup } from '../../models/study-group';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { RouterTestingModule } from '@angular/router/testing';

describe('StudyGroupDetailsComponent', () => {
  let component: StudyGroupDetailsComponent;
  let fixture: ComponentFixture<StudyGroupDetailsComponent>;
  let studyGroupService: jasmine.SpyObj<StudyGroupService>;
  let notificationService: jasmine.SpyObj<StudyGroupNotificationService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: any;

  const mockGroup: StudyGroup = {
    groupId: 1,
    name: 'Group 1',
    level: 'A1',
    status: 'ACTIVE',
    location: 'Loc 1',
    maxCapacity: 10,
    startdate: '2025-01-01',
    enddate: '2025-02-01',
    courseId: 1,
    tutorId: '1'
  };

  beforeEach(() => {
    const studyGroupSpy = jasmine.createSpyObj('StudyGroupService', ['getById']);
    const notificationSpy = jasmine.createSpyObj('StudyGroupNotificationService', [
      'connect', 'disconnect', 'subscribeToGroup', 'sendNewContent', 'sendNewSession', 'sendNewMessage'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    activatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('1')
        }
      }
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [StudyGroupDetailsComponent],
      providers: [
        { provide: StudyGroupService, useValue: studyGroupSpy },
        { provide: StudyGroupNotificationService, useValue: notificationSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    });

    fixture = TestBed.createComponent(StudyGroupDetailsComponent);
    component = fixture.componentInstance;
    studyGroupService = TestBed.inject(StudyGroupService) as jasmine.SpyObj<StudyGroupService>;
    notificationService = TestBed.inject(StudyGroupNotificationService) as jasmine.SpyObj<StudyGroupNotificationService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    studyGroupService.getById.and.returnValue(of(mockGroup));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load group details on init', () => {
    studyGroupService.getById.and.returnValue(of(mockGroup));
    fixture.detectChanges();

    expect(studyGroupService.getById).toHaveBeenCalledWith(1);
    expect(component.group).toEqual(mockGroup);
    expect(notificationService.connect).toHaveBeenCalled();
    expect(notificationService.subscribeToGroup).toHaveBeenCalledWith(1);
  });

  it('should navigate back if ID is invalid', () => {
    activatedRoute.snapshot.paramMap.get.and.returnValue('abc');
    fixture.detectChanges();

    expect(component.error).toBe('ID invalide.');
    expect(router.navigate).toHaveBeenCalledWith(['/study-groups']);
  });

  it('should navigate back if group not found', () => {
    studyGroupService.getById.and.returnValue(throwError(() => new Error('Not Found')));
    fixture.detectChanges();

    expect(component.error).toBe('Groupe introuvable.');
    expect(router.navigate).toHaveBeenCalledWith(['/study-groups']);
  });

  it('should call notification service for new content', () => {
    studyGroupService.getById.and.returnValue(of(mockGroup));
    fixture.detectChanges();
    
    component.onContentAdded('New Doc', 'PDF');
    expect(notificationService.sendNewContent).toHaveBeenCalledWith(1, 'New Doc', 'PDF');
  });

  it('should call notification service for new session', () => {
    studyGroupService.getById.and.returnValue(of(mockGroup));
    fixture.detectChanges();
    
    component.onSessionPlanned('2025-05-01');
    expect(notificationService.sendNewSession).toHaveBeenCalledWith(1, '2025-05-01');
  });

  it('should call notification service for new message', () => {
    studyGroupService.getById.and.returnValue(of(mockGroup));
    fixture.detectChanges();
    
    component.onMessageSent('Alice');
    expect(notificationService.sendNewMessage).toHaveBeenCalledWith(1, 'Alice');
  });

  it('should disconnect on destroy', () => {
    component.ngOnDestroy();
    expect(notificationService.disconnect).toHaveBeenCalled();
  });
});
