import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TakeTestComponent } from './take-test.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppointmentService } from '../../../services/appointment.service';
import { of } from 'rxjs'; // Pour simuler une réponse du serveur

describe('TakeTestComponent Logic', () => {
  let component: TakeTestComponent;
  let fixture: ComponentFixture<TakeTestComponent>;
  let apptService: AppointmentService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TakeTestComponent ],
      imports: [ 
        HttpClientTestingModule, 
        RouterTestingModule, 
        FormsModule, 
        ReactiveFormsModule 
      ],
      providers: [ AppointmentService ] // On fournit le service
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TakeTestComponent);
    component = fixture.componentInstance;
    apptService = TestBed.inject(AppointmentService);

    // Initialisation des questions
    component.questions = [
      { q: 'Q1', options: ['A', 'B'], answer: 'A' },
      { q: 'Q2', options: ['A', 'B'], answer: 'A' },
      { q: 'Q3', options: ['A', 'B'], answer: 'A' },
      { q: 'Q4', options: ['A', 'B'], answer: 'A' }
    ];
    
    // On simule une session active
    component.activeSession = { id: 'test-uuid-123' };

    // --- CRUCIAL : On simule l'appel au serveur ---
    // On dit au test : "Quand on appelle completeAppointment, réponds OK tout de suite"
    spyOn(apptService, 'completeAppointment').and.returnValue(of({ success: true }));
  });

  it('should calculate Advanced level when score is 4/4', () => {
    // GIVEN
    component.score = 4;
    
    // WHEN
    component.finishTest();

    // THEN
    expect(component.finalLevel).toBe('C1 - Advanced');
  });

  it('should increment cheatCount when window loses focus', () => {
    // GIVEN
    component.testFinished = false;
    component.cheatCount = 0;

    // WHEN
    component.onWindowBlur();

    // THEN
    expect(component.cheatCount).toBe(1);
  });
});