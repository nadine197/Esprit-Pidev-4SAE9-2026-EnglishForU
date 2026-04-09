import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudyGroupDetailsComponent } from './study-group-details.component';

describe('StudyGroupDetailsComponent', () => {
  let component: StudyGroupDetailsComponent;
  let fixture: ComponentFixture<StudyGroupDetailsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StudyGroupDetailsComponent]
    });
    fixture = TestBed.createComponent(StudyGroupDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
