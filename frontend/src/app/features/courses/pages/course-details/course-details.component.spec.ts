import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CourseDetailsComponent } from './course-details.component';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('CourseDetailsComponent', () => {
  let component: CourseDetailsComponent;
  let fixture: ComponentFixture<CourseDetailsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [CourseDetailsComponent]
    });
    fixture = TestBed.createComponent(CourseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
