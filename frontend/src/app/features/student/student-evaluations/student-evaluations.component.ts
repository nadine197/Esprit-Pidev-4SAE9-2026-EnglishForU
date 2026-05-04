import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { StudentEvaluation, StudentEvaluationService } from 'src/app/services/student-evaluation.service';

@Component({
  selector: 'app-student-evaluations-page',
  templateUrl: './student-evaluations.component.html'
})
export class StudentEvaluationsPageComponent implements OnInit {
  currentUser: any = null;
  loading = true;
  errorMessage = '';
  evaluations: StudentEvaluation[] = [];

  constructor(
    private authService: AuthService,
    private evaluationService: StudentEvaluationService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.loadEvaluations();
  }

  loadEvaluations() {
    this.loading = true;
    this.evaluationService.getMyEvaluations().subscribe({
      next: (evaluations) => {
        this.evaluations = evaluations;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load my evaluations', error);
        this.errorMessage = 'Unable to load your evaluations right now.';
        this.loading = false;
      }
    });
  }

  trackByEvaluationId(_: number, evaluation: StudentEvaluation) {
    return evaluation.id;
  }
}
