import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { QuizService, Quiz } from 'src/app/services/quiz.service';
import { CourseService } from 'src/app/services/course.service';

interface ScheduledQuiz extends Quiz {
  scheduledAt: Date;
  scheduledKey: string;
  dateLabel: string;
  timeLabel: string;
  timeRangeLabel: string;
  questionCount: number;
}

interface CalendarDay {
  key: string;
  dayNumber: number;
  previewLabel: string;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  quizCount: number;
  previewTitles: string[];
}

@Component({
  selector: 'app-student-quizzes',
  templateUrl: './student-quizzes.html'
})
export class StudentQuizzesComponent implements OnInit {
  currentUser: any = null;
  courses: any[] = [];
  scheduledQuizzes: ScheduledQuiz[] = [];
  calendarDays: CalendarDay[] = [];
  loading = true;
  errorMessage = '';
  selectedDateKey = '';
  calendarCursor = this.startOfMonth(new Date());

  readonly weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  private readonly scheduleStorageKey = 'admin-quiz-schedule-v1';
  
  private readonly monthFormatter = new Intl.DateTimeFormat('en-US', {
    month: 'long', year: 'numeric'
  });
  private readonly dayFormatter = new Intl.DateTimeFormat('en-US', {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric'
  });
  private readonly shortDayFormatter = new Intl.DateTimeFormat('en-US', {
    weekday: 'short', month: 'short', day: 'numeric'
  });
  private readonly timeFormatter = new Intl.DateTimeFormat('en-US', {
    hour: 'numeric', minute: '2-digit'
  });

  constructor(
    private authService: AuthService,
    private quizService: QuizService,
    private courseService: CourseService,
    private router: Router
  ) {}

  get monthLabel(): string {
    return this.monthFormatter.format(this.calendarCursor);
  }

  get selectedDateLabel(): string {
    return this.dayFormatter.format(this.parseDateKey(this.selectedDateKey));
  }

  get selectedDateQuizzes(): ScheduledQuiz[] {
    return this.scheduledQuizzes.filter((quiz) => quiz.scheduledKey === this.selectedDateKey);
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.loadData();
  }

  loadData() {
    this.loading = true;
    
    // Load courses for the grid
    this.courseService.getAll().subscribe({
      next: (data) => {
        this.courses = data;
        
        // After courses, load quizzes for the calendar
        this.quizService.getAllQuizzes().subscribe({
          next: (quizzes) => {
            this.handleLoadedQuizzes(quizzes);
            this.loading = false;
          },
          error: (err) => {
            console.error('Failed to load quizzes', err);
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error('Failed to load courses', err);
        this.errorMessage = 'Unable to load courses right now.';
        this.loading = false;
      }
    });
  }

  private handleLoadedQuizzes(data: Quiz[]) {
    const storedSchedules = this.readStoredSchedules();
    let didChange = false;

    this.scheduledQuizzes = data
      .map((quiz, index) => {
        const quizKey = String(quiz.id);

        if (!storedSchedules[quizKey]) {
          storedSchedules[quizKey] = this.createDefaultScheduleValue(index, quiz.id);
          didChange = true;
        }

        const scheduledAt = this.parseLocalDateTime(storedSchedules[quizKey]);
        const questionCount = quiz.questions?.length ?? 0;

        return {
          ...quiz,
          scheduledAt,
          scheduledKey: this.toDateKey(scheduledAt),
          dateLabel: this.dayFormatter.format(scheduledAt),
          timeLabel: this.timeFormatter.format(scheduledAt),
          timeRangeLabel: this.formatTimeRange(scheduledAt, questionCount),
          questionCount
        } as ScheduledQuiz;
      })
      .sort((left, right) => left.scheduledAt.getTime() - right.scheduledAt.getTime());

    if (didChange) {
      this.persistStoredSchedules(storedSchedules);
    }

    if (!this.selectedDateKey || !this.scheduledQuizzes.some((quiz) => quiz.scheduledKey === this.selectedDateKey)) {
      this.selectedDateKey = this.scheduledQuizzes.length > 0 
        ? this.scheduledQuizzes[0].scheduledKey 
        : this.toDateKey(new Date());
    }

    this.calendarCursor = this.startOfMonth(this.parseDateKey(this.selectedDateKey));
    this.rebuildCalendar();
  }

  changeMonth(offset: number) {
    this.calendarCursor = new Date(
      this.calendarCursor.getFullYear(),
      this.calendarCursor.getMonth() + offset,
      1
    );
    this.rebuildCalendar();
  }

  jumpToToday() {
    const today = new Date();
    this.calendarCursor = this.startOfMonth(today);
    this.selectedDateKey = this.toDateKey(today);
    this.rebuildCalendar();
  }

  selectCalendarDay(day: CalendarDay) {
    this.selectedDateKey = day.key;
    this.rebuildCalendar();
  }

  private rebuildCalendar() {
    const firstOfMonth = this.startOfMonth(this.calendarCursor);
    const startOffset = (firstOfMonth.getDay() + 6) % 7;
    const gridStart = new Date(firstOfMonth);
    gridStart.setDate(firstOfMonth.getDate() - startOffset);

    this.calendarDays = Array.from({ length: 42 }, (_, index) => {
      const date = new Date(gridStart);
      date.setDate(gridStart.getDate() + index);
      const key = this.toDateKey(date);
      const quizzesForDay = this.scheduledQuizzes.filter((quiz) => quiz.scheduledKey === key);

      return {
        key,
        dayNumber: date.getDate(),
        previewLabel: this.shortDayFormatter.format(date),
        isCurrentMonth: date.getMonth() === this.calendarCursor.getMonth(),
        isToday: key === this.toDateKey(new Date()),
        isSelected: key === this.selectedDateKey,
        quizCount: quizzesForDay.length,
        previewTitles: quizzesForDay.slice(0, 2).map((quiz) => quiz.title)
      };
    });
  }

  private readStoredSchedules(): Record<string, string> {
    try {
      const raw = localStorage.getItem(this.scheduleStorageKey);
      return raw ? JSON.parse(raw) as Record<string, string> : {};
    } catch (error) {
      return {};
    }
  }

  private persistStoredSchedules(schedules: Record<string, string>) {
    try {
      localStorage.setItem(this.scheduleStorageKey, JSON.stringify(schedules));
    } catch (error) {
      console.warn('Unable to persist quiz planner schedules', error);
    }
  }

  private createDefaultScheduleValue(index: number, quizId: number): string {
    const baseDate = new Date();
    baseDate.setHours(0, 0, 0, 0);
    const scheduledAt = new Date(baseDate);
    scheduledAt.setDate(baseDate.getDate() + ((index * 2) % 18) + (quizId % 4));
    const hours = [9, 11, 14, 16, 18];
    scheduledAt.setHours(hours[(quizId + index) % hours.length], 0, 0, 0);
    return this.toDateTimeLocalValue(scheduledAt);
  }

  private formatTimeRange(date: Date, questionCount: number): string {
    const durationMinutes = Math.max(20, questionCount * 8);
    const end = new Date(date);
    end.setMinutes(end.getMinutes() + durationMinutes);
    return `${this.timeFormatter.format(date)} - ${this.timeFormatter.format(end)}`;
  }

  private startOfMonth(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  }

  private toDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private toDateTimeLocalValue(date: Date): string {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${this.toDateKey(date)}T${hours}:${minutes}`;
  }

  private parseDateKey(dateKey: string): Date {
    const [year, month, day] = dateKey.split('-').map(Number);
    return new Date(year, (month || 1) - 1, day || 1);
  }

  private parseLocalDateTime(value: string): Date {
    const [datePart, timePart = '09:00'] = value.split('T');
    const [year, month, day] = datePart.split('-').map(Number);
    const [hours, minutes] = timePart.split(':').map(Number);
    return new Date(year || new Date().getFullYear(), (month || 1) - 1, day || 1, hours || 0, minutes || 0);
  }

  startQuiz(id: number) {
    this.router.navigate(['/quizDetails', id]);
  }

  trackByCourseId(_: number, course: any) {
    return course.courseid;
  }

  trackByCalendarDay(_: number, day: CalendarDay) {
    return day.key;
  }

  trackByQuizId(_: number, quiz: Quiz) {
    return quiz.id;
  }
}
