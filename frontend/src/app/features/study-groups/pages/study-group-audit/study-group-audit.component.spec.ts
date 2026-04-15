import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StudyGroupAuditComponent } from './study-group-audit.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, Router } from '@angular/router';

describe('StudyGroupAuditComponent', () => {
  let component: StudyGroupAuditComponent;
  let fixture: ComponentFixture<StudyGroupAuditComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [StudyGroupAuditComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '1' } }
          }
        }
      ]
    });
    fixture = TestBed.createComponent(StudyGroupAuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
