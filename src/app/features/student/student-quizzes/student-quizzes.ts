import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { Quiz, QuizService } from 'src/app/services/quiz.service';

type StudentFilter = 'all' | 'today' | 'this-week' | 'morning' | 'afternoon';

interface StudentScheduledQuiz extends Quiz {
  scheduledAt: Date;
  scheduledKey: string;
  monthLabel: string;
  dayLabel: string;
  weekdayLabel: string;
  fullDateLabel: string;
  timeLabel: string;
  timeRangeLabel: string;
  questionCount: number;
  summary: string;
  accentClass: string;
  badgeClass: string;
  dotClass: string;
}

interface StudentCalendarDay {
  key: string;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  quizCount: number;
  dotClass: string;
}

@Component({
  selector: 'app-student-quizzes',
  templateUrl: './student-quizzes.html'
})
export class StudentQuizzesComponent implements OnInit {
  currentUser: any = null;
  quizzes: Quiz[] = [];
  scheduledQuizzes: StudentScheduledQuiz[] = [];
  calendarDays: StudentCalendarDay[] = [];
  selectedFilter: StudentFilter = 'all';
  selectedDateKey = '';
  calendarCursor = this.startOfMonth(new Date());
  loading = true;
  errorMessage = '';

  readonly weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  readonly filterOptions: Array<{ value: StudentFilter; label: string }> = [
    { value: 'all', label: 'All Quizzes' },
    { value: 'today', label: 'Today' },
    { value: 'this-week', label: 'This Week' },
    { value: 'morning', label: 'Morning' },
    { value: 'afternoon', label: 'Afternoon' }
  ];

  private readonly scheduleStorageKey = 'admin-quiz-schedule-v1';
  private readonly monthFormatter = new Intl.DateTimeFormat('en-US', {
    month: 'long',
    year: 'numeric'
  });
  private readonly longDateFormatter = new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  });
  private readonly monthTagFormatter = new Intl.DateTimeFormat('en-US', {
    month: 'short'
  });
  private readonly weekdayTagFormatter = new Intl.DateTimeFormat('en-US', {
    weekday: 'short'
  });
  private readonly timeFormatter = new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit'
  });

  constructor(
    private authService: AuthService,
    private quizService: QuizService,
    private router: Router
  ) {}

  get monthLabel(): string {
    return this.monthFormatter.format(this.calendarCursor);
  }

  get filteredQuizzes(): StudentScheduledQuiz[] {
    const todayKey = this.toDateKey(new Date());
    const startOfToday = this.parseDateKey(todayKey).getTime();
    const endOfWeek = new Date(startOfToday);
    endOfWeek.setDate(endOfWeek.getDate() + 7);

    return this.scheduledQuizzes.filter((quiz) => {
      switch (this.selectedFilter) {
        case 'today':
          return quiz.scheduledKey === todayKey;
        case 'this-week':
          return quiz.scheduledAt.getTime() >= startOfToday && quiz.scheduledAt.getTime() < endOfWeek.getTime();
        case 'morning':
          return quiz.scheduledAt.getHours() < 12;
        case 'afternoon':
          return quiz.scheduledAt.getHours() >= 12;
        case 'all':
        default:
          return true;
      }
    });
  }

  get selectedDateLabel(): string {
    return this.longDateFormatter.format(this.parseDateKey(this.selectedDateKey));
  }

  get selectedDateQuizzes(): StudentScheduledQuiz[] {
    return this.scheduledQuizzes.filter((quiz) => quiz.scheduledKey === this.selectedDateKey);
  }

  get nextQuizzes(): StudentScheduledQuiz[] {
    const now = Date.now();
    return this.scheduledQuizzes
      .filter((quiz) => quiz.scheduledAt.getTime() >= now)
      .slice(0, 3);
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();

    this.quizService.getAllQuizzes().subscribe({
      next: (data) => {
        this.quizzes = data;
        this.hydrateSchedules();
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load quizzes for student hub', error);
        this.errorMessage = 'Unable to load quizzes right now.';
        this.loading = false;
      }
    });
  }

  selectFilter(filter: StudentFilter) {
    this.selectedFilter = filter;
  }

  selectCalendarDay(day: StudentCalendarDay) {
    this.selectedDateKey = day.key;
    this.rebuildCalendar();
  }

  changeMonth(offset: number) {
    this.calendarCursor = new Date(
      this.calendarCursor.getFullYear(),
      this.calendarCursor.getMonth() + offset,
      1
    );

    const quizInMonth = this.scheduledQuizzes.find((quiz) =>
      quiz.scheduledAt.getMonth() === this.calendarCursor.getMonth() &&
      quiz.scheduledAt.getFullYear() === this.calendarCursor.getFullYear()
    );

    this.selectedDateKey = quizInMonth
      ? quizInMonth.scheduledKey
      : this.toDateKey(this.calendarCursor);

    this.rebuildCalendar();
  }

  jumpToToday() {
    const today = new Date();
    this.calendarCursor = this.startOfMonth(today);
    this.selectedDateKey = this.toDateKey(today);
    this.rebuildCalendar();
  }

  openQuiz(quizId: number) {
    this.router.navigate(['/quizDetails', quizId]);
  }

  trackByQuizId(_: number, quiz: Quiz) {
    return quiz.id;
  }

  trackByDay(_: number, day: StudentCalendarDay) {
    return day.key;
  }

  private hydrateSchedules() {
    const storedSchedules = this.readStoredSchedules();
    let didChange = false;
    const palette = [
      {
        accentClass: 'border-l-[#5b5cf0]',
        badgeClass: 'bg-indigo-50 text-[#5b5cf0]',
        dotClass: 'bg-[#5b5cf0]'
      },
      {
        accentClass: 'border-l-[#fb7185]',
        badgeClass: 'bg-rose-50 text-rose-500',
        dotClass: 'bg-rose-500'
      },
      {
        accentClass: 'border-l-[#14b8a6]',
        badgeClass: 'bg-teal-50 text-teal-600',
        dotClass: 'bg-teal-500'
      },
      {
        accentClass: 'border-l-[#3b82f6]',
        badgeClass: 'bg-blue-50 text-blue-600',
        dotClass: 'bg-blue-500'
      },
      {
        accentClass: 'border-l-[#f59e0b]',
        badgeClass: 'bg-amber-50 text-amber-600',
        dotClass: 'bg-amber-500'
      }
    ];

    this.scheduledQuizzes = this.quizzes
      .map((quiz, index) => {
        const quizKey = String(quiz.id);
        if (!storedSchedules[quizKey]) {
          storedSchedules[quizKey] = this.createDefaultScheduleValue(index, quiz.id);
          didChange = true;
        }

        const scheduledAt = this.parseLocalDateTime(storedSchedules[quizKey]);
        const questionCount = quiz.questions?.length ?? 0;
        const paletteItem = palette[index % palette.length];

        return {
          ...quiz,
          scheduledAt,
          scheduledKey: this.toDateKey(scheduledAt),
          monthLabel: this.monthTagFormatter.format(scheduledAt).toUpperCase(),
          dayLabel: String(scheduledAt.getDate()).padStart(2, '0'),
          weekdayLabel: this.weekdayTagFormatter.format(scheduledAt),
          fullDateLabel: this.longDateFormatter.format(scheduledAt),
          timeLabel: this.timeFormatter.format(scheduledAt),
          timeRangeLabel: this.formatTimeRange(scheduledAt, questionCount),
          questionCount,
          summary: this.buildSummary(questionCount, quiz.passingScore),
          accentClass: paletteItem.accentClass,
          badgeClass: paletteItem.badgeClass,
          dotClass: paletteItem.dotClass
        };
      })
      .sort((left, right) => left.scheduledAt.getTime() - right.scheduledAt.getTime());

    if (didChange) {
      this.persistStoredSchedules(storedSchedules);
    }

    if (!this.scheduledQuizzes.length) {
      this.selectedDateKey = this.toDateKey(new Date());
      this.calendarDays = [];
      return;
    }

    if (!this.selectedDateKey) {
      this.selectedDateKey = this.scheduledQuizzes[0].scheduledKey;
    }

    this.calendarCursor = this.startOfMonth(this.parseDateKey(this.selectedDateKey));
    this.rebuildCalendar();
  }

  private rebuildCalendar() {
    const firstOfMonth = this.startOfMonth(this.calendarCursor);
    const startOffset = (firstOfMonth.getDay() + 6) % 7;
    const gridStart = new Date(firstOfMonth);
    gridStart.setDate(firstOfMonth.getDate() - startOffset);

    this.calendarDays = Array.from({ length: 35 }, (_, index) => {
      const date = new Date(gridStart);
      date.setDate(gridStart.getDate() + index);
      const key = this.toDateKey(date);
      const quizzesForDay = this.scheduledQuizzes.filter((quiz) => quiz.scheduledKey === key);

      return {
        key,
        dayNumber: date.getDate(),
        isCurrentMonth: date.getMonth() === this.calendarCursor.getMonth(),
        isToday: key === this.toDateKey(new Date()),
        isSelected: key === this.selectedDateKey,
        quizCount: quizzesForDay.length,
        dotClass: quizzesForDay[0]?.dotClass ?? 'bg-slate-300'
      };
    });
  }

  private buildSummary(questionCount: number, passingScore: number): string {
    const questionLabel = questionCount === 1 ? 'question' : 'questions';
    return `${questionCount} ${questionLabel} with a ${passingScore}% passing score. Stay focused and complete it in one session.`;
  }

  private readStoredSchedules(): Record<string, string> {
    try {
      const raw = localStorage.getItem(this.scheduleStorageKey);
      return raw ? JSON.parse(raw) as Record<string, string> : {};
    } catch (error) {
      console.warn('Unable to read quiz schedules', error);
      return {};
    }
  }

  private persistStoredSchedules(schedules: Record<string, string>) {
    try {
      localStorage.setItem(this.scheduleStorageKey, JSON.stringify(schedules));
    } catch (error) {
      console.warn('Unable to store quiz schedules', error);
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
