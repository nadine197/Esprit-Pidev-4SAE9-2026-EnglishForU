import { Component, OnInit } from '@angular/core';
import {
  MotivationSuggestionRequest,
  StudentEvaluation,
  StudentEvaluationRequest,
  StudentEvaluationService
} from 'src/app/services/student-evaluation.service';

type EvaluationForm = {
  studentId: string;
  studentName: string;
  studentEmail: string;
  quizAttemptId: string;
  title: string;
  feedback: string;
  strengths: string;
  areasToImprove: string;
  recommendedActions: string;
  rating: string;
};

@Component({
  selector: 'app-student-evaluations',
  templateUrl: './student-evaluations.component.html'
})
export class StudentEvaluationsComponent implements OnInit {
  evaluations: StudentEvaluation[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  editingId: number | null = null;

  // ✅ AI
  aiLoading = false;
  aiSuggestion = '';
  parsedAiSuggestion: any = null;

  form: EvaluationForm = this.createEmptyForm();

  constructor(private evaluationService: StudentEvaluationService) {}

  ngOnInit(): void {
    this.loadEvaluations();
  }

  loadEvaluations() {
    this.loading = true;
    this.errorMessage = '';

    this.evaluationService.getEvaluations().subscribe({
      next: (evaluations) => {
        this.evaluations = evaluations;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load student evaluations', error);
        this.errorMessage = 'Unable to load evaluations right now.';
        this.loading = false;
      }
    });
  }

  startCreate() {
    this.editingId = null;
    this.successMessage = '';
    this.aiSuggestion = '';
    this.form = this.createEmptyForm();
  }

  startEdit(evaluation: StudentEvaluation) {
    this.editingId = evaluation.id;
    this.successMessage = '';
    this.aiSuggestion = '';
    this.form = {
      studentId: evaluation.studentId || '',
      studentName: evaluation.studentName || '',
      studentEmail: evaluation.studentEmail || '',
      quizAttemptId: evaluation.quizAttemptId != null ? String(evaluation.quizAttemptId) : '',
      title: evaluation.title || '',
      feedback: evaluation.feedback || '',
      strengths: evaluation.strengths || '',
      areasToImprove: evaluation.areasToImprove || '',
      recommendedActions: evaluation.recommendedActions || '',
      rating: evaluation.rating != null ? String(evaluation.rating) : ''
    };
  }

  cancelEdit() {
    this.startCreate();
  }

  submit() {
    if (!this.form.studentId || !this.form.studentName || !this.form.studentEmail || !this.form.title || !this.form.feedback) {
      this.errorMessage = 'Student ID, name, email, title, and feedback are required.';
      return;
    }
    if (!this.isValidUuid(this.form.studentId)) {
      this.errorMessage = 'Student UUID is invalid. Use a valid UUID format (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx).';
      return;
    }
    if (this.form.quizAttemptId.trim() && this.parseOptionalNumber(this.form.quizAttemptId) == null) {
      this.errorMessage = 'Quiz Attempt ID must be a number.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload: StudentEvaluationRequest = {
      studentId: this.form.studentId.trim(),
      studentName: this.form.studentName.trim(),
      studentEmail: this.form.studentEmail.trim(),
      quizAttemptId: this.parseOptionalNumber(this.form.quizAttemptId),
      title: this.form.title.trim(),
      feedback: this.form.feedback.trim(),
      strengths: this.parseOptionalText(this.form.strengths),
      areasToImprove: this.parseOptionalText(this.form.areasToImprove),
      recommendedActions: this.parseOptionalText(this.form.recommendedActions),
      rating: this.parseOptionalNumber(this.form.rating)
    };

    const request$ = this.editingId == null
      ? this.evaluationService.createEvaluation(payload)
      : this.evaluationService.updateEvaluation(this.editingId, payload);

    request$.subscribe({
      next: () => {
        this.successMessage = this.editingId == null
          ? 'Evaluation created successfully.'
          : 'Evaluation updated successfully.';
        this.startCreate();
        this.loadEvaluations();
      },
      error: (error) => {
        console.error('Failed to save student evaluation', error);
        const backendMessage = error?.error?.message || error?.error?.error;
        this.errorMessage = backendMessage ? String(backendMessage) : 'Unable to save evaluation.';
      },
      complete: () => {
        this.saving = false;
      }
    });
  }

  removeEvaluation(evaluation: StudentEvaluation) {
    if (!confirm(`Delete evaluation "${evaluation.title}"?`)) {
      return;
    }

    this.evaluationService.deleteEvaluation(evaluation.id).subscribe({
      next: () => {
        if (this.editingId === evaluation.id) {
          this.startCreate();
        }
        this.evaluations = this.evaluations.filter((item) => item.id !== evaluation.id);
      },
      error: (error) => {
        console.error('Failed to delete evaluation', error);
        this.errorMessage = 'Unable to delete this evaluation right now.';
      }
    });
  }

  // ✅ Générer des suggestions AI
  generateMotivationSuggestions() {
    if (!this.form.studentId.trim() || !this.form.studentName.trim() || !this.form.studentEmail.trim()) {
      this.errorMessage = 'Student UUID, name, and email are required before generating AI suggestions.';
      return;
    }

    this.aiLoading = true;
    this.aiSuggestion = '';
    this.parsedAiSuggestion = null;
    this.errorMessage = '';

    const payload: MotivationSuggestionRequest = {
      studentId: this.form.studentId.trim(),
      studentName: this.form.studentName.trim(),
      studentEmail: this.form.studentEmail.trim(),
      englishLevel: null,
      learningGoal: null,
      quizAttemptId: this.parseOptionalNumber(this.form.quizAttemptId),
      title: this.parseOptionalText(this.form.title),
      rating: this.parseOptionalNumber(this.form.rating),
      feedback: this.parseOptionalText(this.form.feedback),
      strengths: this.parseOptionalText(this.form.strengths),
      areasToImprove: this.parseOptionalText(this.form.areasToImprove),
      recommendedActions: this.parseOptionalText(this.form.recommendedActions)
    };

    this.evaluationService.getMotivationSuggestions(payload).subscribe({
      next: (response: any) => {
        const rawSuggestion = response.suggestion || response.message || '';
        this.aiSuggestion = rawSuggestion;
        
        try {
          if (typeof rawSuggestion === 'string' && (rawSuggestion.trim().startsWith('{') || rawSuggestion.trim().startsWith('['))) {
            this.parsedAiSuggestion = JSON.parse(rawSuggestion);
          } else if (typeof rawSuggestion === 'object') {
            this.parsedAiSuggestion = rawSuggestion;
          }
        } catch (e) {
          this.parsedAiSuggestion = null;
        }
        
        this.aiLoading = false;
      },
      error: (error) => {
        console.error('AI suggestion failed', error);
        this.errorMessage = error?.error?.message || 'Unable to generate AI suggestions.';
        this.aiLoading = false;
      }
    });
  }

  trackByEvaluationId(_: number, evaluation: StudentEvaluation) {
    return evaluation.id;
  }

  private createEmptyForm(): EvaluationForm {
    return {
      studentId: '',
      studentName: '',
      studentEmail: '',
      quizAttemptId: '',
      title: '',
      feedback: '',
      strengths: '',
      areasToImprove: '',
      recommendedActions: '',
      rating: ''
    };
  }

  private parseOptionalText(value: string): string | null {
    const normalized = value?.trim();
    return normalized ? normalized : null;
  }

  private parseOptionalNumber(value: string): number | null {
    const normalized = value?.trim();
    if (!normalized) return null;
    const parsed = Number(normalized);
    return Number.isFinite(parsed) ? parsed : null;
  }

  private isValidUuid(value: string): boolean {
    const normalized = value?.trim();
    if (!normalized) return false;
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(normalized);
  }
}
