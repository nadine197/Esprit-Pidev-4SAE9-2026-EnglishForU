import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentListComponent } from './content-list.component';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

describe('ContentListComponent', () => {
  let component: ContentListComponent;
  let fixture: ComponentFixture<ContentListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      declarations: [ContentListComponent]
    });
    fixture = TestBed.createComponent(ContentListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
