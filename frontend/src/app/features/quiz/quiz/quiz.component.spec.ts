import { QuizComponent } from './quiz.component';
import { QuizService } from 'src/app/services/quiz.service';
import { of } from 'rxjs';

describe('QuizComponent', () => {
  let quizService: jasmine.SpyObj<QuizService>;
  let router: { url: string; navigate: jasmine.Spy };
  let route: any;
  let component: QuizComponent;

  beforeEach(() => {
    localStorage.removeItem('admin-quiz-schedule-v1');
    quizService = jasmine.createSpyObj<QuizService>('QuizService', ['getAllQuizzes', 'getQuizzesByCourse']);
    router = {
      url: '/admin/quizzes',
      navigate: jasmine.createSpy('navigate')
    };
    route = {
      snapshot: {
        paramMap: {
          get: () => null
        }
      }
    };
    component = new QuizComponent(quizService, router as any, route);
  });

  afterEach(() => {
    localStorage.removeItem('admin-quiz-schedule-v1');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads all quizzes and builds the admin schedule calendar', () => {
    quizService.getAllQuizzes.and.returnValue(of([
      { id: 1, title: 'Grammar Basics', passingScore: 70, questions: [{ id: 10, text: 'Q1', answers: [] }] },
      { id: 2, title: 'Listening Practice', passingScore: 70, questions: [] }
    ]));

    component.ngOnInit();

    expect(quizService.getAllQuizzes).toHaveBeenCalled();
    expect(component.isAdminQuizContext).toBeTrue();
    expect(component.pageTitle).toBe('Quiz Calendar');
    expect(component.scheduledQuizzes.length).toBe(2);
    expect(component.calendarDays.length).toBe(42);
    expect(component.loading).toBeFalse();
  });

  it('loads quizzes for a specific course when the route contains an id', () => {
    route.snapshot.paramMap.get = () => '3';
    router.url = '/admin/quizzes/course/3';
    quizService.getQuizzesByCourse.and.returnValue(of([
      { id: 7, title: 'Course Quiz', passingScore: 70, questions: [] }
    ]));

    component.ngOnInit();

    expect(quizService.getQuizzesByCourse).toHaveBeenCalledWith(3);
    expect(component.courseId).toBe(3);
    expect(component.pageTitle).toBe('Course #3 Quizzes');
    expect(component.loading).toBeFalse();
  });

  it('navigates to the correct details screen when starting a quiz', () => {
    component.isAdminQuizContext = true;
    component.startQuiz(9);
    expect(router.navigate).toHaveBeenCalledWith(['/admin/quizzes/details', 9]);

    router.navigate.calls.reset();
    component.isAdminQuizContext = false;
    component.startQuiz(9);
    expect(router.navigate).toHaveBeenCalledWith(['/quizDetails', 9]);
  });
});
