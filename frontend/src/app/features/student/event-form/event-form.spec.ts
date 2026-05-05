import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule, AbstractControl, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { EventFormComponent } from './event-form';
import { EventService } from '../../../services/event.service';
import { ImageUploadService } from '../../../services/image-upload.service';

describe('EventFormComponent', () => {
  let component: EventFormComponent;
  let fixture: ComponentFixture<EventFormComponent>;
  let eventServiceSpy: jasmine.SpyObj<EventService>;
  let imageUploadServiceSpy: jasmine.SpyObj<ImageUploadService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const activatedRouteMock = {
    snapshot: {
      paramMap: { get: (_key: string) => null },
      url: []
    }
  };

  beforeEach(async () => {
    eventServiceSpy = jasmine.createSpyObj('EventService', [
      'getEventById', 'createEvent', 'updateEvent'
    ]);
    imageUploadServiceSpy = jasmine.createSpyObj('ImageUploadService', ['uploadImage']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [EventFormComponent],
      imports: [ReactiveFormsModule, FormsModule],
      providers: [
        FormBuilder,
        { provide: EventService, useValue: eventServiceSpy },
        { provide: ImageUploadService, useValue: imageUploadServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(EventFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // --- Form initialization ---
  describe('Form initialization', () => {
    it('should initialize in create mode (isEditMode = false)', () => {
      expect(component.isEditMode).toBeFalse();
    });

    it('should have all required form controls', () => {
      const form = component.eventForm;
      expect(form.get('title')).toBeTruthy();
      expect(form.get('description')).toBeTruthy();
      expect(form.get('eventDate')).toBeTruthy();
      expect(form.get('location')).toBeTruthy();
      expect(form.get('imageUrl')).toBeTruthy();
      expect(form.get('paid')).toBeTruthy();
      expect(form.get('price')).toBeTruthy();
      expect(form.get('maxParticipants')).toBeTruthy();
    });

    it('should start with an invalid form (required fields empty)', () => {
      expect(component.eventForm.invalid).toBeTrue();
    });
  });

  // --- futureDateValidator ---
  describe('futureDateValidator (via eventDate control)', () => {
    let dateControl: AbstractControl;

    beforeEach(() => {
      dateControl = component.eventForm.get('eventDate')!;
    });

    it('should be valid when the date is in the future', () => {
      const future = new Date();
      future.setDate(future.getDate() + 1);
      dateControl.setValue(future.toISOString().slice(0, 16));
      expect(dateControl.errors?.['pastDate']).toBeUndefined();
    });

    it('should return pastDate error when the date is in the past', () => {
      dateControl.setValue('2020-01-01T10:00');
      expect(dateControl.errors?.['pastDate']).toBeTrue();
    });

    it('should be valid when the value is empty (required handles that separately)', () => {
      dateControl.setValue('');
      expect(dateControl.errors?.['pastDate']).toBeUndefined();
      // Required error is present since field is empty
      expect(dateControl.errors?.['required']).toBeTrue();
    });
  });

  // --- Dynamic price validators when paid toggles ---
  describe('paid/price dynamic validators', () => {
    it('should add required + min validators to price when paid is set to true', () => {
      component.eventForm.get('paid')!.setValue(true);

      const priceCtrl = component.eventForm.get('price')!;
      priceCtrl.setValue(null);
      priceCtrl.markAsTouched();

      expect(priceCtrl.errors?.['required']).toBeTrue();
    });

    it('should enforce minimum price of 0.5 when paid is true', () => {
      component.eventForm.get('paid')!.setValue(true);

      const priceCtrl = component.eventForm.get('price')!;
      priceCtrl.setValue(0.1);

      expect(priceCtrl.errors?.['min']).toBeTruthy();
    });

    it('should enforce maximum price of 10000 when paid is true', () => {
      component.eventForm.get('paid')!.setValue(true);

      const priceCtrl = component.eventForm.get('price')!;
      priceCtrl.setValue(15000);

      expect(priceCtrl.errors?.['max']).toBeTruthy();
    });

    it('should accept a valid price between 0.5 and 10000', () => {
      component.eventForm.get('paid')!.setValue(true);

      const priceCtrl = component.eventForm.get('price')!;
      priceCtrl.setValue(50);

      expect(priceCtrl.valid).toBeTrue();
    });

    it('should clear price validators and reset value when paid is toggled back to false', () => {
      component.eventForm.get('paid')!.setValue(true);
      component.eventForm.get('paid')!.setValue(false);

      const priceCtrl = component.eventForm.get('price')!;
      expect(priceCtrl.value).toBeNull();
      expect(priceCtrl.errors).toBeNull();
    });
  });

  // --- isPaid getter ---
  describe('isPaid getter', () => {
    it('should return false by default', () => {
      expect(component.isPaid).toBeFalse();
    });

    it('should return true when paid control is set to true', () => {
      component.eventForm.get('paid')!.setValue(true);
      expect(component.isPaid).toBeTrue();
    });
  });

  // --- minDateTime ---
  describe('minDateTime', () => {
    it('should be at least 30 minutes from now', () => {
      // minDateTime is generated from toISOString() (UTC) — append ':00Z' to force UTC parsing
      const minDate = new Date(component.minDateTime + ':00Z');
      const twentyNineMinFromNow = new Date(Date.now() + 29 * 60 * 1000);
      expect(minDate.getTime()).toBeGreaterThan(twentyNineMinFromNow.getTime());
    });

    it('should return a string in ISO datetime-local format (YYYY-MM-DDTHH:mm)', () => {
      expect(component.minDateTime).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
    });
  });

  // --- fieldError ---
  describe('fieldError', () => {
    function touchAndSet(field: string, value: any) {
      const ctrl = component.eventForm.get(field)!;
      ctrl.setValue(value);
      ctrl.markAsTouched();
    }

    it('should return empty string when field is valid', () => {
      touchAndSet('title', 'Valid Title Here');
      expect(component.fieldError('title')).toBe('');
    });

    it('should return required message when field is empty and touched', () => {
      touchAndSet('title', '');
      expect(component.fieldError('title')).toBe('This field is required.');
    });

    it('should return minlength message when value is too short', () => {
      touchAndSet('title', 'ab');
      expect(component.fieldError('title')).toContain('Minimum');
    });

    it('should return maxlength message when value is too long', () => {
      touchAndSet('title', 'a'.repeat(101));
      expect(component.fieldError('title')).toContain('Maximum');
    });

    it('should return pastDate message for past event date', () => {
      touchAndSet('eventDate', '2020-01-01T10:00');
      expect(component.fieldError('eventDate')).toBe('Event date must be in the future.');
    });

    it('should return price min message when price is below 0.5', () => {
      component.eventForm.get('paid')!.setValue(true);
      touchAndSet('price', 0.1);
      expect(component.fieldError('price')).toBe('Price must be at least 0.5 TND.');
    });

    it('should return price max message when price exceeds 10000', () => {
      component.eventForm.get('paid')!.setValue(true);
      touchAndSet('price', 20000);
      expect(component.fieldError('price')).toBe('Price cannot exceed 10,000 TND.');
    });

    it('should return maxParticipants min message when below 1', () => {
      touchAndSet('maxParticipants', 0);
      expect(component.fieldError('maxParticipants')).toBe('Must be at least 1 participant.');
    });

    it('should return maxParticipants max message when above 10000', () => {
      touchAndSet('maxParticipants', 99999);
      expect(component.fieldError('maxParticipants')).toBe('Cannot exceed 10,000 participants.');
    });

    it('should return empty string for untouched invalid field when submitAttempted is false', () => {
      // Not touched, submitAttempted = false → no error shown
      expect(component.fieldError('title')).toBe('');
    });

    it('should show errors for untouched fields after submitAttempted is set', () => {
      component.submitAttempted = true;
      // title is still empty
      expect(component.fieldError('title')).toBe('This field is required.');
    });
  });

  // --- isFieldInvalid ---
  describe('isFieldInvalid', () => {
    it('should return false for a valid untouched field', () => {
      expect(component.isFieldInvalid('title')).toBeFalse();
    });

    it('should return true for an invalid touched field', () => {
      component.eventForm.get('title')!.markAsTouched();
      expect(component.isFieldInvalid('title')).toBeTrue();
    });

    it('should return true for untouched field when submitAttempted is true', () => {
      component.submitAttempted = true;
      expect(component.isFieldInvalid('title')).toBeTrue();
    });
  });

  // --- onSubmit ---
  describe('onSubmit', () => {
    it('should not call eventService when the form is invalid', () => {
      component.onSubmit();
      expect(eventServiceSpy.createEvent).not.toHaveBeenCalled();
      expect(eventServiceSpy.updateEvent).not.toHaveBeenCalled();
    });

    it('should set submitAttempted to true on submit', () => {
      component.onSubmit();
      expect(component.submitAttempted).toBeTrue();
    });

    it('should call createEvent and navigate on valid form submission (create mode)', () => {
      eventServiceSpy.createEvent.and.returnValue(of({ id: 99 }));

      const future = new Date();
      future.setDate(future.getDate() + 1);

      component.eventForm.setValue({
        title: 'Test Event Title',
        description: 'A description that is long enough to be valid',
        eventDate: future.toISOString().slice(0, 16),
        location: 'Tunis',
        imageUrl: '',
        paid: false,
        price: null,
        maxParticipants: null
      });

      component.onSubmit();

      expect(eventServiceSpy.createEvent).toHaveBeenCalled();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/student/events', 99]);
    });
  });

  // --- goBack ---
  describe('goBack', () => {
    it('should navigate to club detail page when clubId is set', () => {
      component.clubId = 3;
      component.goBack();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/student/clubs', 3]);
    });

    it('should navigate to events list when clubId is null', () => {
      component.clubId = null;
      component.goBack();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/student/events']);
    });
  });
});
