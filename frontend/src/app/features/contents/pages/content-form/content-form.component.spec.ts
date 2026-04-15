import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentFormComponent } from './content-form.component';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';

describe('ContentFormComponent', () => {
  let component: ContentFormComponent;
  let fixture: ComponentFixture<ContentFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule, ReactiveFormsModule],
      declarations: [ContentFormComponent]
    });
    fixture = TestBed.createComponent(ContentFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
