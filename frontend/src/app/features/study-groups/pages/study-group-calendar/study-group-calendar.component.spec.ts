import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudyGroupCalendarComponent } from './study-group-calendar.component';

describe('StudyGroupCalendarComponent', () => {
  let component: StudyGroupCalendarComponent;
  let fixture: ComponentFixture<StudyGroupCalendarComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StudyGroupCalendarComponent]
    });
    fixture = TestBed.createComponent(StudyGroupCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
