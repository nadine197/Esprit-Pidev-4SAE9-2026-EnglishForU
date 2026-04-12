import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudyGroupAuditComponent } from './study-group-audit.component';

describe('StudyGroupAuditComponent', () => {
  let component: StudyGroupAuditComponent;
  let fixture: ComponentFixture<StudyGroupAuditComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StudyGroupAuditComponent]
    });
    fixture = TestBed.createComponent(StudyGroupAuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
