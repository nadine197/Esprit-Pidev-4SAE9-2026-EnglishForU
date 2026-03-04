import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { AuthService } from '../../../services/auth.service';
import { ReportCategory, ReportSeverity, ReportStatus, ReportTicket, ReportsService, UpdateHelpdeskReportPayload } from '../../../services/reports.service';
import { Subject, interval, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export interface ReportWithAge extends ReportTicket {
  ageInHours?: number;
  timeInStatusHours?: number;
  flagStatus?: 'normal' | 'warning' | 'critical';
  shouldShowFlag?: boolean;
}

interface ReportEditDraft {
  title: string;
  description: string;
  category: ReportCategory;
  severity: ReportSeverity;
  status: ReportStatus;
  stepsToReproduce: string;
  expectedResult: string;
  actualResult: string;
  pageUrl: string;
  userAgent: string;
  appVersion: string;
}

@Component({
  selector: 'app-helpdesk-board',
  templateUrl: './helpdesk-board.html',
  styleUrls: ['./helpdesk-board.css']
})
export class HelpdeskBoardComponent implements OnInit, OnDestroy {
  readonly statuses: ReportStatus[] = ['NEW', 'TRIAGED', 'IN_PROGRESS', 'DONE', 'CLOSED'];
  readonly categories: ReportCategory[] = ['BUG', 'ISSUE', 'FEATURE_REQUEST'];
  readonly severityFilters: Array<ReportSeverity | 'ALL'> = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  // Thresholds in hours
  readonly WARNING_THRESHOLD = 24; // Show flag after 24 hours in NEW/TRIAGED
  readonly CRITICAL_THRESHOLD = 48; // Show critical flag after 48 hours
  readonly IN_PROGRESS_WARNING = 48; // Show flag after 48 hours in progress
  readonly IN_PROGRESS_CRITICAL = 72; // Critical after 72 hours

  reports: ReportWithAge[] = [];
  isLoading = false;
  errorMessage = '';

  searchText = '';
  selectedSeverity: ReportSeverity | 'ALL' = 'ALL';
  selectedTicketId?: number;

  isModalOpen = false;
  isSavingModal = false;
  modalErrorMessage = '';
  activeTicket: ReportWithAge | null = null;
  editDraft: ReportEditDraft = this.emptyDraft();

  currentUser: any = null;
  private destroy$ = new Subject<void>();
  private refreshSub?: Subscription;

  constructor(
    private reportsService: ReportsService,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();

    this.route.queryParamMap.subscribe((params) => {
      const ticketId = params.get('ticketId');
      this.selectedTicketId = ticketId ? Number(ticketId) : undefined;
    });

    this.loadReports();

    // Refresh age/flags every 30 seconds
    this.refreshSub = interval(30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.updateReportAges());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.refreshSub?.unsubscribe();
  }

  loadReports(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.reportsService.getHelpdeskReports().subscribe({
      next: (reports) => {
        this.reports = reports as ReportWithAge[];
        if (reports.length === 0) {
          this.reports = this.createMockReports();
        }
        this.updateReportAges();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load helpdesk tickets.';
        this.reports = this.createMockReports();
        this.updateReportAges();
        this.isLoading = false;
      }
    });
  }

  private createMockReports(): ReportWithAge[] {
    const now = new Date();
    const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);
    const twoHoursAgo = new Date(now.getTime() - 2 * 60 * 60 * 1000);
    const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    const twoDaysAgo = new Date(now.getTime() - 48 * 60 * 60 * 1000);
    const threeDaysAgo = new Date(now.getTime() - 72 * 60 * 60 * 1000);

    return [
      {
        id: 1,
        title: 'Sample Bug Report',
        category: 'BUG',
        severity: 'HIGH',
        status: 'NEW',
        description: 'This is a sample bug report for demonstration purposes.',
        shortDescription: 'Sample bug report',
        createdAt: threeDaysAgo.toISOString(),
        updatedAt: threeDaysAgo.toISOString(),
        createdBy: { id: 'user1', name: 'John', lastName: 'Doe', email: 'john.doe@example.com' },
        assignedTo: null,
        ageInHours: 72
      },
      {
        id: 2,
        title: 'Login Page Performance Issue',
        category: 'BUG',
        severity: 'MEDIUM',
        status: 'TRIAGED',
        description: 'Login page takes too long to load',
        shortDescription: 'Performance issue on login',
        createdAt: twoDaysAgo.toISOString(),
        updatedAt: twoDaysAgo.toISOString(),
        createdBy: { id: 'user2', name: 'Jane', lastName: 'Smith', email: 'jane.smith@example.com' },
        assignedTo: { id: 'user3', name: 'Bob', lastName: 'Johnson', email: 'bob.johnson@example.com' },
        ageInHours: 48
      },
      {
        id: 3,
        title: 'Missing Dark Mode Feature',
        category: 'FEATURE_REQUEST',
        severity: 'LOW',
        status: 'IN_PROGRESS',
        description: 'Add dark mode theme to the application',
        shortDescription: 'Dark mode feature request',
        createdAt: oneDayAgo.toISOString(),
        updatedAt: oneDayAgo.toISOString(),
        createdBy: { id: 'user4', name: 'Alice', lastName: 'Williams', email: 'alice.williams@example.com' },
        assignedTo: { id: 'user3', name: 'Bob', lastName: 'Johnson', email: 'bob.johnson@example.com' },
        ageInHours: 24
      },
      {
        id: 4,
        title: 'Database Connection Error',
        category: 'BUG',
        severity: 'CRITICAL',
        status: 'NEW',
        description: 'Database connection fails intermittently',
        shortDescription: 'Critical database issue',
        createdAt: twoHoursAgo.toISOString(),
        updatedAt: twoHoursAgo.toISOString(),
        createdBy: { id: 'user5', name: 'Charlie', lastName: 'Brown', email: 'charlie.brown@example.com' },
        assignedTo: null,
        ageInHours: 2
      },
      {
        id: 5,
        title: 'Typo in Help Section',
        category: 'BUG',
        severity: 'LOW',
        status: 'DONE',
        description: 'Minor typo found in help documentation',
        shortDescription: 'Documentation typo',
        createdAt: oneHourAgo.toISOString(),
        updatedAt: oneHourAgo.toISOString(),
        createdBy: { id: 'user6', name: 'David', lastName: 'Lee', email: 'david.lee@example.com' },
        assignedTo: null,
        ageInHours: 1
      },
      {
        id: 6,
        title: 'API Rate Limiting Implementation',
        category: 'FEATURE_REQUEST',
        severity: 'HIGH',
        status: 'CLOSED',
        description: 'Implement rate limiting for API endpoints',
        shortDescription: 'API rate limiting',
        createdAt: now.toISOString(),
        updatedAt: now.toISOString(),
        createdBy: { id: 'user7', name: 'Eva', lastName: 'Martinez', email: 'eva.martinez@example.com' },
        assignedTo: null,
        ageInHours: 0
      }
    ];
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchText = value.toLowerCase().trim();
  }

  onSeverityFilterChange(event: Event): void {
    this.selectedSeverity = (event.target as HTMLSelectElement).value as ReportSeverity | 'ALL';
  }

  openTicketModal(report: ReportWithAge): void {
    this.activeTicket = { ...report };
    this.editDraft = {
      title: report.title ?? '',
      description: report.description ?? '',
      category: report.category,
      severity: report.severity,
      status: report.status,
      stepsToReproduce: report.stepsToReproduce ?? '',
      expectedResult: report.expectedResult ?? '',
      actualResult: report.actualResult ?? '',
      pageUrl: report.pageUrl ?? '',
      userAgent: report.userAgent ?? '',
      appVersion: report.appVersion ?? ''
    };
    this.modalErrorMessage = '';
    this.isModalOpen = true;
  }

  closeTicketModal(): void {
    this.isModalOpen = false;
    this.isSavingModal = false;
    this.modalErrorMessage = '';
    this.activeTicket = null;
    this.editDraft = this.emptyDraft();
  }

  saveTicketChanges(): void {
    if (!this.activeTicket) {
      return;
    }

    const title = this.editDraft.title.trim();
    const description = this.editDraft.description.trim();
    if (!title || !description) {
      this.modalErrorMessage = 'Title and description are required.';
      return;
    }

    const payload: UpdateHelpdeskReportPayload = {
      title,
      description,
      category: this.editDraft.category,
      severity: this.editDraft.severity,
      status: this.editDraft.status,
      stepsToReproduce: this.editDraft.stepsToReproduce,
      expectedResult: this.editDraft.expectedResult,
      actualResult: this.editDraft.actualResult,
      pageUrl: this.editDraft.pageUrl,
      userAgent: this.editDraft.userAgent,
      appVersion: this.editDraft.appVersion
    };

    this.modalErrorMessage = '';
    this.isSavingModal = true;

    this.reportsService.updateHelpdeskReport(this.activeTicket.id, payload).subscribe({
      next: (updated) => {
        const updatedTicket = updated as ReportWithAge;
        this.replaceReport(updatedTicket);
        this.activeTicket = updatedTicket;
        this.updateReportAges();
        this.isSavingModal = false;
      },
      error: (error) => {
        const backendMessage = error?.error?.message;
        this.modalErrorMessage = backendMessage || 'Failed to save ticket changes.';
        this.isSavingModal = false;
      }
    });
  }

  assignToMeFromModal(): void {
    if (!this.activeTicket) {
      return;
    }

    this.reportsService.updateHelpdeskReport(this.activeTicket.id, { assignToMe: true }).subscribe({
      next: (updated) => {
        const updatedTicket = updated as ReportWithAge;
        this.replaceReport(updatedTicket);
        this.activeTicket = updatedTicket;
        this.updateReportAges();
      }
    });
  }

  unassignFromModal(): void {
    if (!this.activeTicket) {
      return;
    }

    this.reportsService.updateHelpdeskReport(this.activeTicket.id, { unassign: true }).subscribe({
      next: (updated) => {
        const updatedTicket = updated as ReportWithAge;
        this.replaceReport(updatedTicket);
        this.activeTicket = updatedTicket;
        this.updateReportAges();
      }
    });
  }

  // Drag-drop handler
  drop(event: CdkDragDrop<ReportStatus>): void {
    const report = event.item.data as ReportWithAge;
    const newStatus = event.container.data as ReportStatus;

    if (!report || !newStatus || report.status === newStatus) {
      return;
    }

    this.updateReportStatus(report, newStatus);
  }

  private updateReportStatus(report: ReportWithAge, newStatus: ReportStatus): void {
    const previousStatus = report.status;
    report.status = newStatus;

    this.reportsService.updateHelpdeskReport(report.id, { status: newStatus }).subscribe({
      next: (updated) => {
        this.replaceReport(updated as ReportWithAge);
        this.updateReportAges();
      },
      error: () => {
        report.status = previousStatus;
      }
    });
  }

  onStatusChange(report: ReportWithAge, event: Event): void {
    const newStatus = (event.target as HTMLSelectElement).value as ReportStatus;
    if (newStatus === report.status) {
      return;
    }

    this.updateReportStatus(report, newStatus);
  }

  assignToMe(report: ReportWithAge): void {
    this.reportsService.updateHelpdeskReport(report.id, { assignToMe: true }).subscribe({
      next: (updated) => this.replaceReport(updated as ReportWithAge)
    });
  }

  isAssignedToCurrentUser(report: ReportWithAge): boolean {
    return !!report.assignedTo && report.assignedTo.email === this.currentUser?.email;
  }

  getReportsByStatus(status: ReportStatus): ReportWithAge[] {
    return this.getFilteredReports().filter((report) => report.status === status);
  }

  getSeverityClass(severity: ReportSeverity): string {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-100 text-red-700 border border-red-300';
      case 'HIGH':
        return 'bg-orange-100 text-orange-700 border border-orange-300';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-700 border border-yellow-300';
      default:
        return 'bg-slate-100 text-slate-700 border border-slate-300';
    }
  }

  getFlagClass(flagStatus?: string): string {
    switch (flagStatus) {
      case 'critical':
        return 'text-red-600 bg-red-50 border border-red-200';
      case 'warning':
        return 'text-yellow-600 bg-yellow-50 border border-yellow-200';
      default:
        return 'text-slate-400 bg-slate-50 border border-slate-200';
    }
  }

  getAgeDisplayText(ageInHours?: number): string {
    if (!ageInHours) return '';
    if (ageInHours < 1) return '< 1h';
    if (ageInHours < 24) return `${Math.floor(ageInHours)}h`;
    const days = Math.floor(ageInHours / 24);
    return `${days}d`;
  }

  // Calculate and update ages and flag statuses
  private updateReportAges(): void {
    const now = new Date().getTime();

    this.reports.forEach((report) => {
      const createdTime = new Date(report.createdAt).getTime();
      const ageInMs = now - createdTime;
      report.ageInHours = ageInMs / (1000 * 60 * 60);

      // Determine flag status based on status and age
      if (report.status === 'DONE' || report.status === 'CLOSED') {
        report.flagStatus = 'normal';
        report.shouldShowFlag = false;
      } else if (
        report.status === 'NEW' ||
        report.status === 'TRIAGED'
      ) {
        if (report.ageInHours >= this.CRITICAL_THRESHOLD) {
          report.flagStatus = 'critical';
          report.shouldShowFlag = true;
        } else if (report.ageInHours >= this.WARNING_THRESHOLD) {
          report.flagStatus = 'warning';
          report.shouldShowFlag = true;
        } else {
          report.flagStatus = 'normal';
          report.shouldShowFlag = false;
        }
      } else if (report.status === 'IN_PROGRESS') {
        if (report.ageInHours >= this.IN_PROGRESS_CRITICAL) {
          report.flagStatus = 'critical';
          report.shouldShowFlag = true;
        } else if (report.ageInHours >= this.IN_PROGRESS_WARNING) {
          report.flagStatus = 'warning';
          report.shouldShowFlag = true;
        } else {
          report.flagStatus = 'normal';
          report.shouldShowFlag = false;
        }
      }
    });
  }

  trackByReportId(_: number, report: ReportWithAge): number {
    return report.id;
  }

  private getFilteredReports(): ReportWithAge[] {
    return this.reports.filter((report) => {
      const matchesSearch = !this.searchText
        || report.title.toLowerCase().includes(this.searchText)
        || report.description.toLowerCase().includes(this.searchText);

      const matchesSeverity = this.selectedSeverity === 'ALL' || report.severity === this.selectedSeverity;

      return matchesSearch && matchesSeverity;
    });
  }

  private replaceReport(updated: ReportTicket): void {
    this.reports = this.reports.map((report) => report.id === updated.id ? { ...updated as ReportWithAge } : report);
  }

  private emptyDraft(): ReportEditDraft {
    return {
      title: '',
      description: '',
      category: 'BUG',
      severity: 'LOW',
      status: 'NEW',
      stepsToReproduce: '',
      expectedResult: '',
      actualResult: '',
      pageUrl: '',
      userAgent: '',
      appVersion: ''
    };
  }
}
