import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentFormComponent } from './content-form.component';

describe('ContentFormComponent', () => {
  let component: ContentFormComponent;
  let fixture: ComponentFixture<ContentFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
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
