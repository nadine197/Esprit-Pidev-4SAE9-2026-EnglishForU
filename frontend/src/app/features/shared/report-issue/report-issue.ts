import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { CreateReportPayload, ReportCategory, ReportSeverity, ReportsService } from '../../../services/reports.service';

@Component({
  selector: 'app-report-issue',
  templateUrl: './report-issue.html',
  styleUrls: ['./report-issue.css']
})
export class ReportIssueComponent implements OnDestroy {
  isOpen = false;
  isSubmitting = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  categories: ReportCategory[] = ['BUG', 'ISSUE', 'FEATURE_REQUEST'];
  severities: ReportSeverity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  reportForm: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private reportsService: ReportsService,
    private router: Router
  ) {
    this.reportForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(180)]],
      category: ['BUG', [Validators.required]],
      severity: ['MEDIUM', [Validators.required]],
      description: ['', [Validators.required]],
      stepsToReproduce: [''],
      expectedResult: [''],
      actualResult: ['']
    });
  }

  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  openModal(): void {
    this.isOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    if (!this.isSubmitting) {
      this.isOpen = false;
      document.body.style.overflow = '';
    }
  }

  submit(): void {
    if (this.reportForm.invalid) {
      this.reportForm.markAllAsTouched();
      return;
    }

    const formValue = this.reportForm.value;
    const payload: CreateReportPayload = {
      title: formValue.title,
      category: formValue.category,
      severity: formValue.severity,
      description: formValue.description,
      stepsToReproduce: formValue.stepsToReproduce || undefined,
      expectedResult: formValue.expectedResult || undefined,
      actualResult: formValue.actualResult || undefined,
      pageUrl: this.router.url,
      userAgent: navigator.userAgent,
      appVersion: environment.appVersion || undefined
    };

    this.isSubmitting = true;

    this.reportsService.createReport(payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.isOpen = false;
        document.body.style.overflow = '';
        this.reportForm.reset({
          title: '',
          category: 'BUG',
          severity: 'MEDIUM',
          description: '',
          stepsToReproduce: '',
          expectedResult: '',
          actualResult: ''
        });
        this.showToast('Report submitted successfully. Help Desk has been notified.', 'success');
      },
      error: () => {
        this.isSubmitting = false;
        this.showToast('Failed to submit report. Please try again.', 'error');
      }
    });
  }

  getTitleLength(): number {
    return (this.reportForm.get('title')?.value || '').length;
  }

  getDescriptionLength(): number {
    return (this.reportForm.get('description')?.value || '').length;
  }

  getCategoryHint(): string {
    const category = this.reportForm.get('category')?.value as ReportCategory;

    if (category === 'BUG') {
      return 'Use this when something is broken, crashing, or behaving incorrectly.';
    }

    if (category === 'FEATURE_REQUEST') {
      return 'Use this when you want a new feature or enhancement added.';
    }

    return 'Use this for platform problems that are not direct code bugs.';
  }

  getSeverityHint(): string {
    const severity = this.reportForm.get('severity')?.value as ReportSeverity;

    if (severity === 'CRITICAL') {
      return 'Critical issues block major workflows or affect many users.';
    }

    if (severity === 'HIGH') {
      return 'High severity issues have major impact but may have a workaround.';
    }

    if (severity === 'MEDIUM') {
      return 'Medium severity issues are noticeable but not blocking.';
    }

    return 'Low severity is for minor or cosmetic issues.';
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toastMessage = message;
    this.toastType = type;

    setTimeout(() => {
      this.toastMessage = '';
    }, 3000);
  }
}
