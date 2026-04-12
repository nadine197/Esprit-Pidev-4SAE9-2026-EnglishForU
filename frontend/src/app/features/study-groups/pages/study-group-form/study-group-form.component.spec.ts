import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudyGroupFormComponent } from './study-group-form.component';

describe('StudyGroupFormComponent', () => {
  let component: StudyGroupFormComponent;
  let fixture: ComponentFixture<StudyGroupFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StudyGroupFormComponent]
    });
    fixture = TestBed.createComponent(StudyGroupFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
