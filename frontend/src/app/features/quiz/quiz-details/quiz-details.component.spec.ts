import { QuizDetailsComponent } from './quiz-details.component';
import { QuizService } from 'src/app/services/quiz.service';
import { AuthService } from 'src/app/services/auth.service';
import { of } from 'rxjs';

describe('QuizDetailsComponent', () => {
  let quizService: jasmine.SpyObj<QuizService>;
  let authService: jasmine.SpyObj<AuthService>;
  let route: any;
  let router: { url: string; navigate: jasmine.Spy };
  let component: QuizDetailsComponent;

  beforeEach(() => {
    quizService = jasmine.createSpyObj<QuizService>('QuizService', [
      'getQuizById',
      'updateQuiz',
      'deleteQuiz',
      'addQuestion',
      'submitQuiz',
      'generateCourseCertificate'
    ]);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getUser']);
    route = {
      snapshot: {
        paramMap: { get: () => '4' },
        queryParamMap: { get: () => null }
      }
    };
    router = {
      url: '/quizDetails/4',
      navigate: jasmine.createSpy('navigate')
    };
    component = new QuizDetailsComponent(route, router as any, quizService, authService);
    spyOn(console, 'error');
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the quiz and current user on init', () => {
    authService.getUser.and.returnValue({
      id: 'student-1',
      name: 'Amy',
      lastName: 'Stone',
      email: 'amy@example.com'
    });
    quizService.getQuizById.and.returnValue(of({
      id: 4,
      title: 'Grammar Basics',
      passingScore: 70,
      questions: [{ id: 10, text: 'Q1', answers: [{ id: 100, text: 'A', correct: true }] }]
    }));

    component.ngOnInit();

    expect(quizService.getQuizById).toHaveBeenCalledWith(4);
    expect(component.quiz.id).toBe(4);
    expect(component.editedQuiz).not.toBe(component.quiz);
    expect(component.currentUser.email).toBe('amy@example.com');
  });

  it('prevents submission until every question has an answer', () => {
    component.currentUser = {
      id: 'student-1',
      name: 'Amy',
      lastName: 'Stone',
      email: 'amy@example.com'
    };
    component.quiz = {
      id: 4,
      questions: [
        { id: 10, answers: [{ id: 100 }] },
        { id: 11, answers: [{ id: 110 }] }
      ]
    };
    component.selectedAnswers = { 10: 100 };

    component.submitQuiz();

    expect(quizService.submitQuiz).not.toHaveBeenCalled();
    expect(component.noticeMessage).toBe('Please answer every question before submitting.');
    expect(component.noticeTone).toBe('error');
  });

  it('submits a quiz and stores the completion details', () => {
    component.currentUser = {
      id: 'student-1',
      name: 'Amy',
      lastName: 'Stone',
      email: 'amy@example.com'
    };
    component.quiz = {
      id: 4,
      questions: [
        { id: 10, answers: [{ id: 100 }, { id: 101 }] }
      ]
    };
    component.selectedAnswers = { 10: 100 };
    quizService.submitQuiz.and.returnValue(of({
      passed: false,
      score: 60,
      attemptAnswers: [
        { questionId: 10, correctAnswerId: 101 }
      ]
    }));

    component.submitQuiz();

    expect(quizService.submitQuiz).toHaveBeenCalledWith(jasmine.objectContaining({
      quizId: 4,
      studentId: 'student-1',
      studentName: 'Amy Stone',
      studentEmail: 'amy@example.com',
      answers: { 10: 100 }
    }));
    expect(component.noticeMessage).toBe('Your responses were saved successfully.');
    expect(component.completionTitle).toBe('Quiz submitted');
    expect(component.correctAnswersMap[10]).toBe(101);
    expect(component.showCorrectAnswerMap[10]).toBeTrue();
    expect(component.submitDisabled).toBeFalse();
  });

  it('saves the edited quiz and leaves edit mode', () => {
    component.quiz = { id: 4, title: 'Grammar Basics', questions: [] };
    component.editedQuiz = { id: 4, title: 'Updated Quiz', questions: [] };
    component.isEditMode = true;
    quizService.updateQuiz.and.returnValue(of({
      id: 4,
      title: 'Updated Quiz',
      passingScore: 70,
      questions: []
    }));

    component.saveQuiz();

    expect(quizService.updateQuiz).toHaveBeenCalledWith(4, jasmine.objectContaining({
      title: 'Updated Quiz'
    }));
    expect(component.quiz.title).toBe('Updated Quiz');
    expect(component.isEditMode).toBeFalse();
    expect(component.noticeTone).toBe('success');
  });

  it('downloads and emails the certificate after a passing quiz', () => {
    component.currentUser = {
      id: '1f0d8eb7-5367-4e05-92d2-bd9d3e9a1c1c',
      name: 'Amy',
      lastName: 'Stone',
      email: 'amy@example.com'
    };
    component.quiz = {
      id: 4,
      questions: [
        { id: 10, answers: [{ id: 100 }, { id: 101 }] }
      ]
    };
    component.selectedAnswers = { 10: 100 };

    const pdfBlob = new Blob(['pdf'], { type: 'application/pdf' });
    quizService.submitQuiz.and.returnValue(of({
      passed: true,
      score: 90,
      attemptAnswers: []
    }));
    quizService.generateCourseCertificate.and.returnValue(of({
      body: pdfBlob,
      headers: {
        get: () => 'attachment; filename="english-certificate.pdf"'
      }
    } as any));

    localStorage.setItem('USER_ID', '1f0d8eb7-5367-4e05-92d2-bd9d3e9a1c1c');
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:certificate');
    spyOn(window.URL, 'revokeObjectURL');
    const anchor = document.createElement('a');
    spyOn(document, 'createElement').and.returnValue(anchor);
    spyOn(anchor, 'click');

    component.submitQuiz();

    expect(quizService.generateCourseCertificate).toHaveBeenCalledWith(jasmine.objectContaining({
      quizId: 4,
      studentEmail: 'amy@example.com'
    }));
    expect(anchor.download).toBe('english-certificate.pdf');
    expect(component.noticeMessage).toBe('Your certificate PDF was downloaded and emailed with a QR containing the certificate information.');
  });
});
