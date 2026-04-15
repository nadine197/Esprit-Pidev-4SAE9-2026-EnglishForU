import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StudyGroupStatsComponent } from './study-group-stats.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('StudyGroupStatsComponent', () => {
  let component: StudyGroupStatsComponent;
  let fixture: ComponentFixture<StudyGroupStatsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [StudyGroupStatsComponent]
    });
    fixture = TestBed.createComponent(StudyGroupStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
