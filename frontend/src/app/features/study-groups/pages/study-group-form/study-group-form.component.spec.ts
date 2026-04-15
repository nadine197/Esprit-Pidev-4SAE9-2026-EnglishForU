import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StudyGroupFormComponent } from './study-group-form.component';
import { StudyGroupService } from '../../../../services/study-group.service';
import { Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { StudyGroup } from '../../models/study-group';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('StudyGroupFormComponent', () => {
  let component: StudyGroupFormComponent;
  let fixture: ComponentFixture<StudyGroupFormComponent>;
  let studyGroupService: jasmine.SpyObj<StudyGroupService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: any;

  const mockGroup: StudyGroup = {
    groupId: 123,
    name: 'Test Group',
    level: 'A1',
    location: 'Test Loc',
    maxCapacity: 5,
    startdate: '2025-01-01',
    enddate: '2025-02-01',
    status: 'ACTIVE',
    courseId: 1,
    tutorId: '12345678-1234-1234-1234-1234567890ab',
    studentsIds: ['12345678-1234-1234-1234-1234567890ac']
  };

  beforeEach(() => {
    const studyGroupSpy = jasmine.createSpyObj('StudyGroupService', ['getById', 'create', 'update']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    activatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue(null)
        }
      }
    };

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, HttpClientTestingModule],
      declarations: [StudyGroupFormComponent],
      providers: [
        FormBuilder,
        { provide: StudyGroupService, useValue: studyGroupSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ]
    });

    fixture = TestBed.createComponent(StudyGroupFormComponent);
    component = fixture.componentInstance;
    studyGroupService = TestBed.inject(StudyGroupService) as jasmine.SpyObj<StudyGroupService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should initialize in create mode', () => {
    fixture.detectChanges();
    expect(component.isEditMode).toBeFalse();
    expect(component.groupForm.get('status')?.value).toBe('ACTIVE');
  });

  it('should initialize in edit mode and load group data', () => {
    activatedRoute.snapshot.paramMap.get.and.returnValue('123');
    studyGroupService.getById.and.returnValue(of(mockGroup));
    
    fixture.detectChanges();

    expect(component.isEditMode).toBeTrue();
    expect(component.groupId).toBe(123);
    expect(studyGroupService.getById).toHaveBeenCalledWith(123);
    expect(component.groupForm.get('name')?.value).toBe('Test Group');
    expect(component.groupForm.get('tutorId')?.value).toBe(mockGroup.tutorId);
  });

  it('should validate form fields', () => {
    fixture.detectChanges();
    const form = component.groupForm;
    
    form.get('name')?.setValue('');
    expect(form.get('name')?.invalid).toBeTrue();

    form.get('maxCapacity')?.setValue(0);
    expect(form.get('maxCapacity')?.invalid).toBeTrue();

    form.get('tutorId')?.setValue('invalid-uuid');
    expect(form.get('tutorId')?.invalid).toBeTrue();

    form.get('tutorId')?.setValue('12345678-1234-1234-1234-1234567890ab');
    expect(form.get('tutorId')?.valid).toBeTrue();
  });

  it('should call create service on submit in create mode', () => {
    fixture.detectChanges();
    component.groupForm.patchValue({
      ...mockGroup,
      studentsIds: '12345678-1234-1234-1234-1234567890ac, 12345678-1234-1234-1234-1234567890ad'
    });
    studyGroupService.create.and.returnValue(of(mockGroup));

    component.onSubmit();

    expect(studyGroupService.create).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/study-groups']);
  });

  it('should call update service on submit in edit mode', () => {
    activatedRoute.snapshot.paramMap.get.and.returnValue('123');
    studyGroupService.getById.and.returnValue(of(mockGroup));
    fixture.detectChanges();

    component.groupForm.patchValue({ name: 'Updated Name' });
    studyGroupService.update.and.returnValue(of(mockGroup));

    component.onSubmit();

    expect(studyGroupService.update).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/study-groups']);
  });

  it('should handle submission error', () => {
    fixture.detectChanges();
    component.groupForm.patchValue({
      ...mockGroup,
      studentsIds: mockGroup.studentsIds?.join(', ') ?? ''
    });
    const errorMsg = 'Server internal error';
    studyGroupService.create.and.returnValue(throwError(() => ({ error: { message: errorMsg } })));

    component.onSubmit();

    expect(component.submitError).toBe(errorMsg);
  });

  it('should mark all fields as touched if form is invalid on submit', () => {
    fixture.detectChanges();
    component.onSubmit();
    expect(component.groupForm.touched).toBeTrue();
  });
});
