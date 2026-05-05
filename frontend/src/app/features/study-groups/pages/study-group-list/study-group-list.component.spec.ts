import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { StudyGroupListComponent } from './study-group-list.component';
import { StudyGroupService } from '../../../../services/study-group.service';
import { StudyGroupNotificationService } from '../../../../services/study-group-notification.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of, Subject } from 'rxjs';
import { StudyGroup } from '../../models/study-group';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { RouterTestingModule } from '@angular/router/testing';

describe('StudyGroupListComponent', () => {
  let component: StudyGroupListComponent;
  let fixture: ComponentFixture<StudyGroupListComponent>;
  let studyGroupService: jasmine.SpyObj<StudyGroupService>;
  let notificationService: jasmine.SpyObj<StudyGroupNotificationService>;
  let router: jasmine.SpyObj<Router>;

  const mockGroups: StudyGroup[] = [
    { groupId: 1, name: 'Group 1', level: 'A1', status: 'ACTIVE', location: 'Loc 1', maxCapacity: 10, startdate: '', enddate: '', courseId: 1, tutorId: '1' },
    { groupId: 2, name: 'Group 2', level: 'B2', status: 'PLANNED', location: 'Loc 2', maxCapacity: 20, startdate: '', enddate: '', courseId: 2, tutorId: '2' }
  ];

  beforeEach(() => {
    const studyGroupSpy = jasmine.createSpyObj('StudyGroupService', ['getAll', 'search', 'delete']);
    const notificationSpy = jasmine.createSpyObj('StudyGroupNotificationService', [], {
      notifications$: new Subject()
    });

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, HttpClientTestingModule, RouterTestingModule],
      declarations: [StudyGroupListComponent],
      providers: [
        FormBuilder,
        { provide: StudyGroupService, useValue: studyGroupSpy },
        { provide: StudyGroupNotificationService, useValue: notificationSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    });

    fixture = TestBed.createComponent(StudyGroupListComponent);
    component = fixture.componentInstance;
    studyGroupService = TestBed.inject(StudyGroupService) as jasmine.SpyObj<StudyGroupService>;
    notificationService = TestBed.inject(StudyGroupNotificationService) as jasmine.SpyObj<StudyGroupNotificationService>;
    const routerInstance = TestBed.inject(Router);
    spyOn(routerInstance, 'navigate');
    router = routerInstance as jasmine.SpyObj<Router>;

    studyGroupService.getAll.and.returnValue(of(mockGroups));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load groups on init', () => {
    expect(studyGroupService.getAll).toHaveBeenCalled();
    expect(component.groups.length).toBe(2);
    expect(component.allGroups.length).toBe(2);
  });

  it('should apply filters and call search service', fakeAsync(() => {
    studyGroupService.search.and.returnValue(of([mockGroups[0]]));
    
    component.filterForm.patchValue({ name: 'Group 1' });
    tick(400); // Wait for debounceTime
    fixture.detectChanges();

    expect(studyGroupService.search).toHaveBeenCalledWith(jasmine.objectContaining({ name: 'Group 1' }));
    expect(component.groups.length).toBe(1);
    expect(component.groups[0].name).toBe('Group 1');
  }));

  it('should reset filters', () => {
    component.filterForm.patchValue({ name: 'Test' });
    component.resetFilters();
    expect(component.filterForm.value.name).toBe('');
    expect(component.groups).toEqual(component.allGroups);
  });

  it('should delete group after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    studyGroupService.delete.and.returnValue(of(undefined));
    
    component.delete(1);

    expect(window.confirm).toHaveBeenCalled();
    expect(studyGroupService.delete).toHaveBeenCalledWith(1);
    expect(studyGroupService.getAll).toHaveBeenCalledTimes(2); // Initial + After delete
  });

  it('should not delete group if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    
    component.delete(1);

    expect(studyGroupService.delete).not.toHaveBeenCalled();
  });

  it('should navigate to calendar', () => {
    component.openCalendar();
    expect(router.navigate).toHaveBeenCalledWith(['/study-groups/calendar']);
  });

  it('should navigate to stats', () => {
    component.openStats();
    expect(router.navigate).toHaveBeenCalledWith(['/study-groups/stats']);
  });

  it('should toggle chatbot visibility', () => {
    expect(component.showChatbot).toBeFalse();
    component.toggleChatbot();
    expect(component.showChatbot).toBeTrue();
  });
});
