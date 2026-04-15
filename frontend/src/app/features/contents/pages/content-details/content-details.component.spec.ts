import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentDetailsComponent } from './content-details.component';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('ContentDetailsComponent', () => {
  let component: ContentDetailsComponent;
  let fixture: ComponentFixture<ContentDetailsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [ContentDetailsComponent]
    });
    fixture = TestBed.createComponent(ContentDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
