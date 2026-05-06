import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { QuizService, QuizSubmissionPayload } from 'src/app/services/quiz.service';

@Component({
  selector: 'app-quiz-details',
  templateUrl: './quiz-details.component.html',
  styleUrls: ['./quiz-details.component.css']
})
export class QuizDetailsComponent implements OnInit {
  quiz: any = { questions: [] };
  editedQuiz: any = { questions: [] };
  currentUser: any = null;

  isEditMode = false;
  isAdminQuizContext = false;

  selectedAnswers: Record<number, number> = {};
  correctAnswersMap: Record<number, number> = {};
  showCorrectAnswerMap: Record<number, boolean> = {};

  submitDisabled = false;
  passedQuiz = false;

  certificateDownloading = false;

  noticeMessage = '';
  noticeTone = 'info';

  completionMessage = '';
  completionTitle = '';

  cooldownRemaining = 0;
  timerDisplay = '00:00';
  timerTone = 'calm';

  aiRecommendation: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const editMode = this.route.snapshot.queryParamMap.get('edit');
    const created = this.route.snapshot.queryParamMap.get('created');

    this.currentUser = this.authService.getUser();
    this.isAdminQuizContext = this.router.url.includes('/admin/') || this.router.url.includes('/tutor/');
    this.isEditMode = editMode === '1';

    if (this.isEditMode) {
      this.isAdminQuizContext = true;
    }

    if (created === '1') {
      this.noticeMessage = 'Quiz created successfully. You can add questions now.';
      this.noticeTone = 'success';
    }

    this.quizService.getQuizById(id).subscribe({
      next: (quiz) => {
        this.quiz = quiz;
        this.editedQuiz = JSON.parse(JSON.stringify(quiz));
      },
      error: (error) => {
        console.error('Failed to load quiz', error);
        this.noticeMessage = 'Unable to load this quiz right now.';
        this.noticeTone = 'error';
      }
    });
  }

  get allQuestionsAnswered(): boolean {
    const questions = this.quiz?.questions || [];
    return questions.length > 0 && questions.every((question: any) => !!this.selectedAnswers[question.id]);
  }

  selectAnswer(questionId: number, answerId: number): void {
    this.selectedAnswers[questionId] = answerId;
  }

  toggleShowCorrectAnswer(questionId: number): void {
    this.showCorrectAnswerMap[questionId] = !this.showCorrectAnswerMap[questionId];
  }

  enableEditMode(): void {
    this.isEditMode = true;
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.editedQuiz = JSON.parse(JSON.stringify(this.quiz));
  }

  saveQuiz(): void {
    const payload = JSON.parse(JSON.stringify(this.editedQuiz));

    this.quizService.updateQuiz(this.quiz.id, payload).subscribe({
      next: (savedQuiz) => {
        this.quiz = savedQuiz;
        this.editedQuiz = JSON.parse(JSON.stringify(savedQuiz));
        this.isEditMode = false;
        this.noticeMessage = 'Quiz saved successfully.';
        this.noticeTone = 'success';
      },
      error: (error) => {
        console.error('Failed to save quiz', error);
        this.noticeMessage = 'Unable to save this quiz right now.';
        this.noticeTone = 'error';
      }
    });
  }

  deleteQuiz(): void {
    if (!confirm('Delete this quiz?')) {
      return;
    }

    this.quizService.deleteQuiz(this.quiz.id).subscribe({
      next: () => this.router.navigate(['/admin/quizzes']),
      error: (error) => {
        console.error('Failed to delete quiz', error);
        this.noticeMessage = 'Unable to delete this quiz right now.';
        this.noticeTone = 'error';
      }
    });
  }

  addQuestion(): void {
    const newQuestion = {
      text: 'New question',
      answers: []
    };

    this.quizService.addQuestion(this.quiz.id, newQuestion).subscribe({
      next: (saved) => {
        this.editedQuiz.questions.push({ ...saved, answers: [] });
        this.quiz.questions.push({ ...saved, answers: [] });
        this.noticeMessage = 'Question added.';
        this.noticeTone = 'success';
      },
      error: (error) => {
        console.error('Failed to add question', error);
        this.noticeMessage = 'Unable to add the question.';
        this.noticeTone = 'error';
      }
    });
  }

  addAnswer(question: any): void {
    question.answers.push({
      text: '',
      correct: false
    });
  }

  deleteAnswer(questionIndex: number, answerIndex: number): void {
    this.editedQuiz.questions[questionIndex].answers.splice(answerIndex, 1);
  }

  deleteQuestion(_: number, index: number): void {
    this.editedQuiz.questions.splice(index, 1);
  }

  autoSaveQuestion(_: any): void {}

  autoSaveAnswer(_: any): void {}

  setEditedAnswerCorrectness(answer: any, value: boolean): void {
    answer.correct = value;
  }

  saveQuestion(question: any): void {
    console.log('Saved question:', question);
  }

  submitQuiz(): void {
    if (this.isAdminQuizContext) {
      return;
    }

    const studentId = this.resolveStudentId();
    if (!this.quiz?.id || !studentId) {
      this.noticeMessage = 'You must be logged in to submit this quiz.';
      this.noticeTone = 'error';
      return;
    }

    if (!(this.quiz?.questions || []).length) {
      this.noticeMessage = 'This quiz does not have any questions yet.';
      this.noticeTone = 'error';
      return;
    }

    if (!this.allQuestionsAnswered) {
      this.noticeMessage = 'Please answer every question before submitting.';
      this.noticeTone = 'error';
      return;
    }

    const payload: QuizSubmissionPayload = {
      quizId: this.quiz.id,
      studentId,
      studentName: this.buildStudentName(),
      studentEmail: this.currentUser.email || '',
      answers: this.selectedAnswers
    };

    this.submitDisabled = true;
    this.noticeMessage = '';
    this.completionMessage = '';
    this.completionTitle = '';

    const request = this.quizService.submitQuiz(payload).subscribe({
      next: (attempt) => {
        this.passedQuiz = !!attempt?.passed;
        this.noticeMessage = 'Your responses were saved successfully.';
        this.noticeTone = 'success';
        this.completionTitle = this.passedQuiz ? 'Quiz completed' : 'Quiz submitted';
        this.completionMessage = this.passedQuiz
          ? `Great work. You scored ${attempt?.score ?? 0}% and passed this quiz.`
          : `You scored ${attempt?.score ?? 0}%. Review the correct answers below and try again when you are ready.`;

        const attemptAnswers = Array.isArray(attempt?.attemptAnswers) ? attempt.attemptAnswers : [];
        attemptAnswers.forEach((answer: any) => {
          if (answer?.questionId != null && answer?.correctAnswerId != null) {
            this.correctAnswersMap[answer.questionId] = answer.correctAnswerId;
            this.showCorrectAnswerMap[answer.questionId] = true;
          }
        });

        if (this.passedQuiz) {
          this.generateCertificate();
        }
      },
      error: (error) => {
        console.error('Failed to submit quiz', error);
        const backendMessage =
          error?.error?.message ||
          error?.error?.error ||
          (Array.isArray(error?.error?.errors) ? error.error.errors.join(', ') : null);
        this.noticeMessage = backendMessage
          ? String(backendMessage)
          : 'Unable to submit your quiz right now.';
        this.noticeTone = 'error';
      }
    });

    request.add(() => {
      this.submitDisabled = false;
    });
  }

  generateCertificate(): void {
    const studentId = this.resolveStudentId();
    if (!this.quiz?.id || !studentId) {
      this.noticeMessage = 'You must be logged in to download a certificate.';
      this.noticeTone = 'error';
      return;
    }

    if (!this.passedQuiz) {
      this.noticeMessage = 'You need a score of 70% or more before downloading a certificate.';
      this.noticeTone = 'error';
      return;
    }

    const payload = {
      quizId: this.quiz.id,
      studentId,
      studentName: this.buildStudentName(),
      studentEmail: this.currentUser.email || ''
    };

    this.certificateDownloading = true;
    this.quizService.generateCourseCertificate(payload).subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          this.noticeMessage = 'Certificate could not be generated.';
          this.noticeTone = 'error';
          return;
        }

        const fileName = this.extractFileNameFromContentDisposition(response.headers.get('content-disposition'))
          || 'course-certificate.pdf';

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);

        this.noticeMessage = 'Your certificate PDF was downloaded and emailed with a QR containing the certificate information.';
        this.noticeTone = 'success';
      },
      error: (error) => {
        console.error('Failed to generate certificate', error);
        this.noticeMessage = error?.error?.message
          ? String(error.error.message)
          : 'Unable to generate your certificate right now.';
        this.noticeTone = 'error';
      },
      complete: () => {
        this.certificateDownloading = false;
      }
    });
  }

  generateAiRecommendation(): void {
    this.aiRecommendation = {
      questionText: 'AI Question?',
      answers: [
        { text: 'Yes', correct: true },
        { text: 'No', correct: false }
      ]
    };
  }

  applyAiRecommendation(): void {
    this.editedQuiz.questions.push(this.aiRecommendation);
    this.aiRecommendation = null;
  }

  private buildStudentName(): string {
    return [this.currentUser?.name, this.currentUser?.lastName]
      .filter(Boolean)
      .join(' ')
      .trim();
  }

  private extractFileNameFromContentDisposition(contentDisposition: string | null): string | null {
    if (!contentDisposition) {
      return null;
    }

    const match = /filename\*?=(?:UTF-8''|\")?([^;\"\n]+)(?:\"|;|$)/i.exec(contentDisposition);
    if (!match?.[1]) {
      return null;
    }

    const fileName = match[1].trim();
    try {
      return decodeURIComponent(fileName);
    } catch {
      return fileName;
    }
  }

  private resolveStudentId(): string | null {
    const storedId = localStorage.getItem('USER_ID');
    if (storedId && this.isUuidLike(storedId)) {
      return storedId;
    }

    const userId = this.currentUser?.id != null ? String(this.currentUser.id) : null;
    if (userId && this.isUuidLike(userId)) {
      return userId;
    }

    return null;
  }

  private isUuidLike(value: string): boolean {
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value.trim());
  }
}
