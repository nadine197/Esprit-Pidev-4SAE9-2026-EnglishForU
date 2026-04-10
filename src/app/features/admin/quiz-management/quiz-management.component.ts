import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Course, CourseService } from 'src/app/services/courses.service';
import { QuizService } from 'src/app/services/quiz.service';
import { catchError, forkJoin, map, of } from 'rxjs';

type SortOption =
  | 'title-asc'
  | 'title-desc'
  | 'duration-asc'
  | 'duration-desc'
  | 'courseid-asc'
  | 'courseid-desc';

interface ManagedCourse extends Course {
  durationLabel: string;
  durationValue: number;
  quizCount: number;
  shortTitle: string;
  searchText: string;
}

interface CourseQuizStat {
  rank: number;
  courseId: number;
  title: string;
  shortTitle: string;
  quizCount: number;
  percentage: number;
  barHeight: number;
  color: string;
  gradient: string;
  badgeBackground: string;
  shadowColor: string;
}

@Component({
  selector: 'app-quiz-management',
  templateUrl: './quiz-management.component.html'
})
export class QuizManagementComponent implements OnInit {
  courses: ManagedCourse[] = [];
  loading = true;
  errorMessage = '';
  searchQuery = '';
  showStatsPanel = false;
  selectedSort: SortOption = 'title-asc';

  private readonly statPalette = [
    {
      color: '#2563eb',
      gradient: 'linear-gradient(180deg, #60a5fa 0%, #2563eb 100%)',
      badgeBackground: 'rgba(37, 99, 235, 0.12)',
      shadowColor: 'rgba(37, 99, 235, 0.28)'
    },
    {
      color: '#0f766e',
      gradient: 'linear-gradient(180deg, #34d399 0%, #0f766e 100%)',
      badgeBackground: 'rgba(15, 118, 110, 0.12)',
      shadowColor: 'rgba(15, 118, 110, 0.24)'
    },
    {
      color: '#7c3aed',
      gradient: 'linear-gradient(180deg, #a78bfa 0%, #7c3aed 100%)',
      badgeBackground: 'rgba(124, 58, 237, 0.12)',
      shadowColor: 'rgba(124, 58, 237, 0.25)'
    },
    {
      color: '#ea580c',
      gradient: 'linear-gradient(180deg, #fb923c 0%, #ea580c 100%)',
      badgeBackground: 'rgba(234, 88, 12, 0.12)',
      shadowColor: 'rgba(234, 88, 12, 0.22)'
    },
    {
      color: '#db2777',
      gradient: 'linear-gradient(180deg, #f472b6 0%, #db2777 100%)',
      badgeBackground: 'rgba(219, 39, 119, 0.12)',
      shadowColor: 'rgba(219, 39, 119, 0.24)'
    },
    {
      color: '#0891b2',
      gradient: 'linear-gradient(180deg, #22d3ee 0%, #0891b2 100%)',
      badgeBackground: 'rgba(8, 145, 178, 0.12)',
      shadowColor: 'rgba(8, 145, 178, 0.22)'
    }
  ];

  readonly sortOptions: Array<{ value: SortOption; label: string }> = [
    { value: 'title-asc', label: 'Name A to Z' },
    { value: 'title-desc', label: 'Name Z to A' },
    { value: 'duration-asc', label: 'Duration Low to High' },
    { value: 'duration-desc', label: 'Duration High to Low' },
    { value: 'courseid-asc', label: 'Course ID Low to High' },
    { value: 'courseid-desc', label: 'Course ID High to Low' }
  ];

  constructor(
    private courseService: CourseService,
    private quizService: QuizService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.courseService.getAllCourses().subscribe({
      next: (courses) => {
        this.loadCoursesWithQuizCounts(courses);
      },
      error: (error) => {
        console.error('Failed to load courses for quiz management', error);
        this.errorMessage = 'Unable to load courses right now.';
        this.loading = false;
      }
    });
  }

  get filteredCourses(): ManagedCourse[] {
    const query = this.searchQuery.trim().toLowerCase();
    const filtered = query
      ? this.courses.filter((course) => course.searchText.includes(query))
      : [...this.courses];

    return filtered.sort((left, right) => this.compareCourses(left, right, this.selectedSort));
  }

  get hasActiveFilters(): boolean {
    return !!this.searchQuery.trim() || this.selectedSort !== 'title-asc';
  }

  get totalQuizCount(): number {
    return this.courses.reduce((total, course) => total + course.quizCount, 0);
  }

  get visibleQuizCount(): number {
    return this.filteredCourses.reduce((total, course) => total + course.quizCount, 0);
  }

  get coursesWithQuizzesCount(): number {
    return this.courses.filter((course) => course.quizCount > 0).length;
  }

  get rankedCourseStats(): CourseQuizStat[] {
    const visibleCoursesWithQuizzes = this.filteredCourses
      .filter((course) => course.quizCount > 0)
      .sort((left, right) => right.quizCount - left.quizCount || left.title.localeCompare(right.title))
      .slice(0, 6);

    const maxQuizCount = Math.max(...visibleCoursesWithQuizzes.map((course) => course.quizCount), 1);
    const visibleQuizTotal = visibleCoursesWithQuizzes.reduce((total, course) => total + course.quizCount, 0) || 1;

    return visibleCoursesWithQuizzes.map((course, index) => {
      const palette = this.statPalette[index % this.statPalette.length];

      return {
        rank: index + 1,
        courseId: course.courseid,
        title: course.title,
        shortTitle: course.shortTitle,
        quizCount: course.quizCount,
        percentage: Math.round((course.quizCount / visibleQuizTotal) * 100),
        barHeight: 118 + Math.round((course.quizCount / maxQuizCount) * 172),
        color: palette.color,
        gradient: palette.gradient,
        badgeBackground: palette.badgeBackground,
        shadowColor: palette.shadowColor
      };
    });
  }

  get leadingCourseStat(): CourseQuizStat | null {
    return this.rankedCourseStats[0] ?? null;
  }

  onSearchChange(event: Event) {
    const target = event.target as HTMLInputElement | null;
    this.searchQuery = target?.value ?? '';
  }

  onSortChange(event: Event) {
    const target = event.target as HTMLSelectElement | null;
    this.selectedSort = (target?.value as SortOption) || 'title-asc';
  }

  resetFilters() {
    this.searchQuery = '';
    this.selectedSort = 'title-asc';
  }

  toggleStatsPanel() {
    this.showStatsPanel = !this.showStatsPanel;
  }

  manageCourseQuizzes(courseId: number) {
    this.router.navigate(['/admin/quizzes/course', courseId]);
  }

  showAllQuizzes() {
    this.router.navigate(['/admin/quizzes/list']);
  }

  trackByCourseId(_: number, course: ManagedCourse) {
    return course.courseid;
  }

  trackByStatCourseId(_: number, stat: CourseQuizStat) {
    return stat.courseId;
  }

  private loadCoursesWithQuizCounts(courses: Course[]) {
    if (!courses.length) {
      this.courses = [];
      this.loading = false;
      return;
    }

    const quizCountRequests = courses.map((course) =>
      this.quizService.getQuizzesByCourse(course.courseid).pipe(
        map((quizzes) => quizzes.length),
        catchError((error) => {
          console.error(`Failed to load quizzes for course ${course.courseid}`, error);
          return of(0);
        })
      )
    );

    forkJoin(quizCountRequests).subscribe({
      next: (quizCounts) => {
        this.courses = courses.map((course, index) => this.toManagedCourse(course, quizCounts[index] ?? 0));
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to build quiz management stats', error);
        this.errorMessage = 'Unable to load quiz statistics right now.';
        this.loading = false;
      }
    });
  }

  private toManagedCourse(course: Course, quizCount: number): ManagedCourse {
    const durationLabel = this.formatDuration(course.duration);
    const durationValue = this.parseDuration(course.duration);

    return {
      ...course,
      durationLabel,
      durationValue,
      quizCount,
      shortTitle: this.buildShortTitle(course.title),
      searchText: [course.title, course.description, course.courseid, `${quizCount}`]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
    };
  }

  private buildShortTitle(title: string): string {
    const trimmed = (title || '').trim();
    if (trimmed.length <= 18) {
      return trimmed;
    }

    return `${trimmed.slice(0, 16).trimEnd()}...`;
  }

  private formatDuration(duration: string | number | null | undefined): string {
    if (duration === null || duration === undefined) {
      return 'Duration unavailable';
    }

    const raw = String(duration).trim();
    if (!raw) {
      return 'Duration unavailable';
    }

    return /h/i.test(raw) ? raw : `${raw}h`;
  }

  private parseDuration(duration: string | number | null | undefined): number {
    if (duration === null || duration === undefined) {
      return 0;
    }

    const normalized = String(duration).replace(',', '.');
    const match = normalized.match(/\d+(\.\d+)?/);
    return match ? Number(match[0]) : 0;
  }

  private compareCourses(left: ManagedCourse, right: ManagedCourse, sort: SortOption): number {
    switch (sort) {
      case 'title-desc':
        return right.title.localeCompare(left.title);
      case 'duration-asc':
        return left.durationValue - right.durationValue || left.title.localeCompare(right.title);
      case 'duration-desc':
        return right.durationValue - left.durationValue || left.title.localeCompare(right.title);
      case 'courseid-asc':
        return left.courseid - right.courseid;
      case 'courseid-desc':
        return right.courseid - left.courseid;
      case 'title-asc':
      default:
        return left.title.localeCompare(right.title);
    }
  }
}
