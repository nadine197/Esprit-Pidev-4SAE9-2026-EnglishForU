import { AddQuizComponent } from './add-quiz.component';
import { QuizService } from 'src/app/services/quiz.service';
import { of } from 'rxjs';

describe('AddQuizComponent', () => {
  let quizService: jasmine.SpyObj<QuizService>;
  let route: any;
  let router: { navigate: jasmine.Spy };
  let component: AddQuizComponent;

  beforeEach(() => {
    quizService = jasmine.createSpyObj<QuizService>('QuizService', [
      'addQuiz',
      'addQuestion',
      'addAnswer',
      'updateAnswer',
      'getQuizRecommendation'
    ]);
    route = {
      snapshot: {
        paramMap: {
          get: () => '12'
        }
      }
    };
    router = {
      navigate: jasmine.createSpy('navigate')
    };
    component = new AddQuizComponent(route, quizService, router as any);
    spyOn(window, 'setTimeout').and.returnValue(0 as any);
    spyOn(console, 'error');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('syncs the route course id and quiz title control on init', () => {
    component.ngOnInit();
    component.titleControl.setValue('Grammar Basics');

    expect(component.courseId).toBe(12);
    expect(component.quiz.title).toBe('Grammar Basics');
  });

  it('creates a quiz when the title is valid', () => {
    quizService.addQuiz.and.returnValue(of({
      id: 77,
      title: 'Grammar Basics',
      passingScore: 70,
      questions: []
    }));
    component.ngOnInit();
    component.titleControl.setValue('Grammar Basics');

    component.createQuiz();

    expect(quizService.addQuiz).toHaveBeenCalledWith(12, jasmine.objectContaining({
      title: 'Grammar Basics',
      courseId: 12
    }));
    expect(component.quiz.id).toBe(77);
    expect(component.feedbackTone).toBe('success');
  });

  it('adds a question to the created quiz', () => {
    component.quiz = {
      id: 77,
      title: 'Grammar Basics',
      passingScore: 70,
      questions: []
    };
    component.questionControl.setValue('What is a verb?');
    quizService.addQuestion.and.returnValue(of({
      id: 1,
      text: 'What is a verb?',
      answers: []
    }));

    component.addQuestion();

    expect(quizService.addQuestion).toHaveBeenCalledWith(77, jasmine.objectContaining({
      text: 'What is a verb?'
    }));
    expect(component.quiz.questions.length).toBe(1);
    expect(component.currentQuestionIndex).toBe(0);
    expect(component.feedbackTone).toBe('success');
  });

  it('shows an error when generating an AI recommendation before the quiz exists', () => {
    component.quiz.id = 0;

    component.generateAiRecommendation();

    expect(quizService.getQuizRecommendation).not.toHaveBeenCalled();
    expect(component.feedbackTone).toBe('error');
    expect(component.feedbackMessage).toContain('Cr');
  });
});
