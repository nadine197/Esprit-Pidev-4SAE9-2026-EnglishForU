import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService, Quiz } from 'src/app/services/quiz.service';

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
  selector: 'app-quiz',
  templateUrl: './quiz.component.html'
})
export class QuizComponent implements OnInit {

  quizzes: Quiz[] = [];
  scheduledQuizzes: ScheduledQuiz[] = [];
  calendarDays: CalendarDay[] = [];
  courseId: number | null = null;
  isAdminQuizContext = false;
  pageTitle = 'Available Quizzes';
  pageSubtitle = 'Browse quizzes and open the one you want to manage.';
  loading = true;
  errorMessage = '';
  selectedDateKey = '';
  calendarCursor = this.startOfMonth(new Date());
  editingQuizId: number | null = null;
  scheduleDraftValue = '';
  upcomingPage = 1;

  readonly upcomingPageSize = 5;
  readonly weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  private readonly scheduleStorageKey = 'admin-quiz-schedule-v1';
  private readonly monthFormatter = new Intl.DateTimeFormat('en-US', {
    month: 'long',
    year: 'numeric'
  });
  private readonly dayFormatter = new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  });
  private readonly shortDayFormatter = new Intl.DateTimeFormat('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric'
  });
  private readonly timeFormatter = new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit'
  });

  constructor(
    private quizService: QuizService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  get isAdminScheduleView(): boolean {
    return this.isAdminQuizContext && this.courseId === null;
  }

  get monthLabel(): string {
    return this.monthFormatter.format(this.calendarCursor);
  }

  get selectedDateLabel(): string {
    return this.dayFormatter.format(this.parseDateKey(this.selectedDateKey));
  }

  get selectedDateQuizzes(): ScheduledQuiz[] {
    return this.scheduledQuizzes.filter((quiz) => quiz.scheduledKey === this.selectedDateKey);
  }

  get upcomingQuizzes(): ScheduledQuiz[] {
    const now = Date.now();
    return this.scheduledQuizzes
      .filter((quiz) => quiz.scheduledAt.getTime() >= now);
  }

  get paginatedUpcomingQuizzes(): ScheduledQuiz[] {
    const startIndex = (this.upcomingPage - 1) * this.upcomingPageSize;
    return this.upcomingQuizzes.slice(startIndex, startIndex + this.upcomingPageSize);
  }

  get totalUpcomingPages(): number {
    return Math.max(1, Math.ceil(this.upcomingQuizzes.length / this.upcomingPageSize));
  }

  get upcomingRangeStart(): number {
    if (!this.upcomingQuizzes.length) {
      return 0;
    }

    return (this.upcomingPage - 1) * this.upcomingPageSize + 1;
  }

  get upcomingRangeEnd(): number {
    if (!this.upcomingQuizzes.length) {
      return 0;
    }

    return Math.min(this.upcomingPage * this.upcomingPageSize, this.upcomingQuizzes.length);
  }

  ngOnInit(): void {
    this.isAdminQuizContext = this.router.url.startsWith('/admin/quizzes');
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.courseId = +id;
      this.pageTitle = `Course #${this.courseId} Quizzes`;
      this.pageSubtitle = 'Manage quizzes attached to this course.';
      this.quizService.getQuizzesByCourse(this.courseId).subscribe({
        next: (data) => {
          this.handleLoadedQuizzes(data);
        },
        error: (error) => {
          console.error('Failed to load course quizzes', error);
          this.errorMessage = 'Unable to load quizzes for this course.';
          this.loading = false;
        }
      });
      return;
    }

    this.pageTitle = this.isAdminQuizContext ? 'Quiz Calendar' : this.pageTitle;
    this.pageSubtitle = this.isAdminQuizContext
      ? 'See every quiz on a monthly planner, review the day agenda, and keep the schedule organized.'
      : this.pageSubtitle;

    this.quizService.getAllQuizzes().subscribe({
      next: (data) => {
        this.handleLoadedQuizzes(data);
      },
      error: (error) => {
        console.error('Failed to load quizzes', error);
        this.errorMessage = 'Unable to load quizzes right now.';
        this.loading = false;
      }
    });
  }

  startQuiz(id: number) {
    if (this.isAdminQuizContext) {
      this.router.navigate(['/admin/quizzes/details', id]);
      return;
    }

    this.router.navigate(['/quizDetails', id]);
  }

  openQuizCreation() {
    this.router.navigate(['/admin/quizzes']);
  }

  changeMonth(offset: number) {
    this.calendarCursor = new Date(
      this.calendarCursor.getFullYear(),
      this.calendarCursor.getMonth() + offset,
      1
    );

    const firstQuizInMonth = this.scheduledQuizzes.find((quiz) =>
      quiz.scheduledAt.getMonth() === this.calendarCursor.getMonth() &&
      quiz.scheduledAt.getFullYear() === this.calendarCursor.getFullYear()
    );

    this.selectedDateKey = firstQuizInMonth
      ? firstQuizInMonth.scheduledKey
      : this.toDateKey(this.calendarCursor);

    this.editingQuizId = null;
    this.rebuildCalendar();
  }

  jumpToToday() {
    const today = new Date();
    this.calendarCursor = this.startOfMonth(today);
    this.selectedDateKey = this.toDateKey(today);
    this.editingQuizId = null;
    this.rebuildCalendar();
  }

  selectCalendarDay(day: CalendarDay) {
    this.selectedDateKey = day.key;
    this.editingQuizId = null;
    this.rebuildCalendar();
  }

  openScheduleEditor(quiz: ScheduledQuiz) {
    this.editingQuizId = quiz.id;
    this.scheduleDraftValue = this.toDateTimeLocalValue(quiz.scheduledAt);
  }

  updateScheduleDraft(event: Event) {
    const target = event.target as HTMLInputElement | null;
    this.scheduleDraftValue = target?.value ?? '';
  }

  saveSchedule(quiz: ScheduledQuiz) {
    if (!this.scheduleDraftValue) {
      return;
    }

    const storedSchedules = this.readStoredSchedules();
    storedSchedules[String(quiz.id)] = this.scheduleDraftValue;
    this.persistStoredSchedules(storedSchedules);

    const scheduledAt = this.parseLocalDateTime(this.scheduleDraftValue);
    this.calendarCursor = this.startOfMonth(scheduledAt);
    this.selectedDateKey = this.toDateKey(scheduledAt);
    this.editingQuizId = null;
    this.scheduleDraftValue = '';
    this.hydrateSchedules();
  }

  cancelScheduleEditor() {
    this.editingQuizId = null;
    this.scheduleDraftValue = '';
  }

  goToUpcomingPage(page: number) {
    if (page < 1 || page > this.totalUpcomingPages || page === this.upcomingPage) {
      return;
    }

    this.upcomingPage = page;
  }

  goToPreviousUpcomingPage() {
    this.goToUpcomingPage(this.upcomingPage - 1);
  }

  goToNextUpcomingPage() {
    this.goToUpcomingPage(this.upcomingPage + 1);
  }

  trackByQuizId(_: number, quiz: Quiz) {
    return quiz.id;
  }

  trackByCalendarDay(_: number, day: CalendarDay) {
    return day.key;
  }

  private handleLoadedQuizzes(data: Quiz[]) {
    this.quizzes = data;
    this.hydrateSchedules();
    this.loading = false;
  }

  private hydrateSchedules() {
    const storedSchedules = this.readStoredSchedules();
    let didChange = false;

    this.scheduledQuizzes = this.quizzes
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
        };
      })
      .sort((left, right) => left.scheduledAt.getTime() - right.scheduledAt.getTime());

    if (didChange) {
      this.persistStoredSchedules(storedSchedules);
    }

    if (!this.scheduledQuizzes.length) {
      this.calendarDays = [];
      this.selectedDateKey = this.toDateKey(new Date());
      this.upcomingPage = 1;
      return;
    }

    this.syncUpcomingPage();

    if (!this.selectedDateKey || !this.scheduledQuizzes.some((quiz) => quiz.scheduledKey === this.selectedDateKey)) {
      this.selectedDateKey = this.scheduledQuizzes[0].scheduledKey;
    }

    if (this.isAdminScheduleView) {
      this.calendarCursor = this.startOfMonth(this.parseDateKey(this.selectedDateKey));
      this.rebuildCalendar();
    }
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

  private syncUpcomingPage() {
    if (!this.upcomingQuizzes.length) {
      this.upcomingPage = 1;
      return;
    }

    this.upcomingPage = Math.min(this.upcomingPage, this.totalUpcomingPages);
    this.upcomingPage = Math.max(this.upcomingPage, 1);
  }

  private readStoredSchedules(): Record<string, string> {
    try {
      const raw = localStorage.getItem(this.scheduleStorageKey);
      return raw ? JSON.parse(raw) as Record<string, string> : {};
    } catch (error) {
      console.warn('Unable to read quiz planner schedules', error);
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
    const minutes = [0, 15, 30, 45];
    scheduledAt.setHours(
      hours[(quizId + index) % hours.length],
      minutes[(quizId + index) % minutes.length],
      0,
      0
    );

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

    return new Date(
      year || new Date().getFullYear(),
      (month || 1) - 1,
      day || 1,
      hours || 0,
      minutes || 0,
      0,
      0
    );
  }
}
