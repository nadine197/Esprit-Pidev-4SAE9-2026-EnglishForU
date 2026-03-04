import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ReportSeverity, ReportStatus, ReportTicket, ReportsService } from '../../../services/reports.service';

@Component({
  selector: 'app-helpdesk-board',
  templateUrl: './helpdesk-board.html',
  styleUrls: ['./helpdesk-board.css']
})
export class HelpdeskBoardComponent implements OnInit {
  readonly statuses: ReportStatus[] = ['NEW', 'TRIAGED', 'IN_PROGRESS', 'DONE', 'CLOSED'];
  readonly severityFilters: Array<ReportSeverity | 'ALL'> = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  reports: ReportTicket[] = [];
  isLoading = false;
  errorMessage = '';

  searchText = '';
  selectedSeverity: ReportSeverity | 'ALL' = 'ALL';
  selectedTicketId?: number;

  currentUser: any = null;

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
  }

  loadReports(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.reportsService.getHelpdeskReports().subscribe({
      next: (reports) => {
        this.reports = reports;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load helpdesk tickets.';
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

  onStatusChange(report: ReportTicket, event: Event): void {
    const newStatus = (event.target as HTMLSelectElement).value as ReportStatus;
    if (newStatus === report.status) {
      return;
    }

    const previousStatus = report.status;
    report.status = newStatus;

    this.reportsService.updateHelpdeskReport(report.id, { status: newStatus }).subscribe({
      next: (updated) => this.replaceReport(updated),
      error: () => {
        report.status = previousStatus;
      }
    });
  }

  assignToMe(report: ReportTicket): void {
    this.reportsService.updateHelpdeskReport(report.id, { assignToMe: true }).subscribe({
      next: (updated) => this.replaceReport(updated)
    });
  }

  isAssignedToCurrentUser(report: ReportTicket): boolean {
    return !!report.assignedTo && report.assignedTo.email === this.currentUser?.email;
  }

  getReportsByStatus(status: ReportStatus): ReportTicket[] {
    return this.getFilteredReports().filter((report) => report.status === status);
  }

  getSeverityClass(severity: ReportSeverity): string {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-100 text-red-700';
      case 'HIGH':
        return 'bg-orange-100 text-orange-700';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-700';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  trackByReportId(_: number, report: ReportTicket): number {
    return report.id;
  }

  private getFilteredReports(): ReportTicket[] {
    return this.reports.filter((report) => {
      const matchesSearch = !this.searchText
        || report.title.toLowerCase().includes(this.searchText)
        || report.description.toLowerCase().includes(this.searchText);

      const matchesSeverity = this.selectedSeverity === 'ALL' || report.severity === this.selectedSeverity;

      return matchesSearch && matchesSeverity;
    });
  }

  private replaceReport(updated: ReportTicket): void {
    this.reports = this.reports.map((report) => report.id === updated.id ? updated : report);
  }
}
