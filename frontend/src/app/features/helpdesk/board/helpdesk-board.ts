import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { AuthService } from '../../../services/auth.service';
import { ReportActivity, ReportCategory, ReportComment, ReportSeverity, ReportStatus, ReportTicket, ReportsService, UpdateHelpdeskReportPayload } from '../../../services/reports.service';
import { Subject, forkJoin, interval, Subscription } from 'rxjs';
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
  readonly allowedTransitions: Record<ReportStatus, ReportStatus[]> = {
    NEW: ['TRIAGED', 'IN_PROGRESS', 'CLOSED'],
    TRIAGED: ['IN_PROGRESS', 'DONE', 'CLOSED'],
    IN_PROGRESS: ['DONE', 'CLOSED', 'TRIAGED'],
    DONE: ['CLOSED', 'IN_PROGRESS'],
    CLOSED: ['TRIAGED']
  };

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
  reportComments: ReportComment[] = [];
  reportActivity: ReportActivity[] = [];
  isTimelineLoading = false;
  commentDraft = '';
  requestInfoDraft = '';
  isSendingComment = false;
  isSendingRequestInfo = false;

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
        this.updateReportAges();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to load helpdesk tickets.';
        this.reports = [];
        this.isLoading = false;
      }
    });
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
    this.commentDraft = '';
    this.requestInfoDraft = '';
    this.isModalOpen = true;
    this.loadReportTimeline(report.id);
  }

  closeTicketModal(): void {
    this.isModalOpen = false;
    this.isSavingModal = false;
    this.isTimelineLoading = false;
    this.isSendingComment = false;
    this.isSendingRequestInfo = false;
    this.modalErrorMessage = '';
    this.activeTicket = null;
    this.editDraft = this.emptyDraft();
    this.reportComments = [];
    this.reportActivity = [];
    this.commentDraft = '';
    this.requestInfoDraft = '';
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

    if (
      this.activeTicket.status !== this.editDraft.status
      && !this.isTransitionAllowed(this.activeTicket.status, this.editDraft.status)
    ) {
      this.modalErrorMessage = `Transition from ${this.activeTicket.status} to ${this.editDraft.status} is not allowed.`;
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
        this.loadReportTimeline(updatedTicket.id);
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

    this.modalErrorMessage = '';
    this.reportsService.updateHelpdeskReport(this.activeTicket.id, { assignToMe: true }).subscribe({
      next: (updated) => {
        const updatedTicket = updated as ReportWithAge;
        this.replaceReport(updatedTicket);
        this.activeTicket = updatedTicket;
        this.updateReportAges();
        this.loadReportTimeline(updatedTicket.id);
      },
      error: (error) => {
        this.modalErrorMessage = error?.error?.message || 'Failed to assign this ticket.';
      }
    });
  }

  unassignFromModal(): void {
    if (!this.activeTicket) {
      return;
    }

    this.modalErrorMessage = '';
    this.reportsService.updateHelpdeskReport(this.activeTicket.id, { unassign: true }).subscribe({
      next: (updated) => {
        const updatedTicket = updated as ReportWithAge;
        this.replaceReport(updatedTicket);
        this.activeTicket = updatedTicket;
        this.updateReportAges();
        this.loadReportTimeline(updatedTicket.id);
      },
      error: (error) => {
        this.modalErrorMessage = error?.error?.message || 'Failed to unassign this ticket.';
      }
    });
  }

  addCommentFromModal(): void {
    if (!this.activeTicket || this.isSendingComment) {
      return;
    }

    const message = this.commentDraft.trim();
    if (!message) {
      this.modalErrorMessage = 'Comment message cannot be empty.';
      return;
    }

    this.modalErrorMessage = '';
    this.isSendingComment = true;

    this.reportsService.addHelpdeskReportComment(this.activeTicket.id, { message }).subscribe({
      next: () => {
        this.commentDraft = '';
        this.isSendingComment = false;
        this.loadReportTimeline(this.activeTicket!.id);
      },
      error: (error) => {
        this.modalErrorMessage = error?.error?.message || 'Failed to add comment.';
        this.isSendingComment = false;
      }
    });
  }

  requestInfoFromModal(): void {
    if (!this.activeTicket || this.isSendingRequestInfo) {
      return;
    }

    const requestInfoMessage = this.requestInfoDraft.trim();
    if (!requestInfoMessage) {
      this.modalErrorMessage = 'Request information message cannot be empty.';
      return;
    }

    this.modalErrorMessage = '';
    this.isSendingRequestInfo = true;

    this.reportsService.updateHelpdeskReport(this.activeTicket.id, { requestInfoMessage }).subscribe({
      next: (updated) => {
        const updatedTicket = updated as ReportWithAge;
        this.replaceReport(updatedTicket);
        this.activeTicket = updatedTicket;
        this.updateReportAges();
        this.requestInfoDraft = '';
        this.isSendingRequestInfo = false;
        this.loadReportTimeline(updatedTicket.id);
      },
      error: (error) => {
        this.modalErrorMessage = error?.error?.message || 'Failed to request additional information.';
        this.isSendingRequestInfo = false;
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
    if (!this.isTransitionAllowed(previousStatus, newStatus)) {
      this.errorMessage = `Transition from ${previousStatus} to ${newStatus} is not allowed.`;
      return;
    }

    this.errorMessage = '';
    report.status = newStatus;

    this.reportsService.updateHelpdeskReport(report.id, { status: newStatus }).subscribe({
      next: (updated) => {
        this.replaceReport(updated as ReportWithAge);
        this.updateReportAges();
      },
      error: (error) => {
        report.status = previousStatus;
        this.errorMessage = error?.error?.message || 'Unable to update ticket status.';
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
    this.errorMessage = '';
    this.reportsService.updateHelpdeskReport(report.id, { assignToMe: true }).subscribe({
      next: (updated) => this.replaceReport(updated as ReportWithAge),
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Unable to assign this ticket.';
      }
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

  private loadReportTimeline(reportId: number): void {
    this.isTimelineLoading = true;

    forkJoin({
      comments: this.reportsService.getHelpdeskReportComments(reportId),
      activity: this.reportsService.getHelpdeskReportActivity(reportId)
    }).subscribe({
      next: ({ comments, activity }) => {
        this.reportComments = comments;
        this.reportActivity = activity;
        this.isTimelineLoading = false;
      },
      error: () => {
        this.reportComments = [];
        this.reportActivity = [];
        this.isTimelineLoading = false;
      }
    });
  }

  private isTransitionAllowed(current: ReportStatus, next: ReportStatus): boolean {
    if (current === next) {
      return true;
    }

    return this.allowedTransitions[current]?.includes(next) || false;
  }

  getActivityLabel(type: ReportActivity['type']): string {
    switch (type) {
      case 'REPORT_CREATED':
        return 'Ticket created';
      case 'STATUS_CHANGED':
        return 'Status changed';
      case 'ASSIGNED':
        return 'Assigned';
      case 'UNASSIGNED':
        return 'Unassigned';
      case 'COMMENT_ADDED':
        return 'Comment added';
      case 'REQUEST_INFO':
        return 'Request info';
      default:
        return type;
    }
  }

  getUserDisplayName(name?: string | null, lastName?: string | null, fallback?: string | null): string {
    const fullName = `${name ?? ''} ${lastName ?? ''}`.trim();
    return fullName || fallback || 'Unknown user';
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
