import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, map, switchMap } from 'rxjs';
import { Answer, Question, Quiz, QuizRecommendation, QuizService } from 'src/app/services/quiz.service';

@Component({
  selector: 'app-quiz-details',
  templateUrl: './quiz-details.component.html'
})
export class QuizDetailsComponent implements OnInit, OnDestroy {
  quiz: Quiz = { id: 0, title: '', passingScore: 0, questions: [] };
  selectedAnswers: { [questionId: number]: number } = {};
  showCorrectAnswerMap: { [questionId: number]: boolean } = {};
  correctAnswersMap: { [questionId: number]: Answer[] } = {};
  userRole = '';
  passedQuiz = false;
  quizScore: number | null = null;
  completionTitle = '';
  completionMessage = '';
  attemptCount = 0;
  submitDisabled = false;
  cooldownRemaining = 0;
  isGeneratingCertificate = false;
  isGeneratingAiRecommendation = false;
  isApplyingAiRecommendation = false;
  isEditMode = false;
  editedQuiz!: Quiz;
  aiRecommendation: QuizRecommendation | null = null;
  isAdminQuizContext = false;
  noticeMessage = '';
  noticeTone: 'success' | 'error' | 'info' = 'info';
  deleteConfirmationRequired = false;
  autoOpenEditMode = false;
  showCreationWelcome = false;
  highlightedQuestionId: number | null = null;
  quizTimeLimitSeconds = 3000;
  remainingSeconds = 0;
  timerDisplay = '00:00';
  timerTone: 'calm' | 'warning' | 'critical' = 'calm';
  isAutoSubmittingQuiz = false;

  private cooldownInterval: ReturnType<typeof setInterval> | null = null;
  private quizTimerInterval: ReturnType<typeof setInterval> | null = null;
  private noticeTimeout: ReturnType<typeof setTimeout> | null = null;
  private highlightTimeout: ReturnType<typeof setTimeout> | null = null;
  private studentId = '';
  private readonly retryCooldownSeconds = 30;

  constructor(
    private route: ActivatedRoute,
    private quizService: QuizService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isAdminQuizContext = this.router.url.startsWith('/admin/quizzes');
    const storedRole = localStorage.getItem('ROLE');
    this.userRole = storedRole ? storedRole.replace(/"/g, '') : '';
    this.studentId = this.resolveStudentId();
    this.autoOpenEditMode = this.route.snapshot.queryParamMap.get('edit') === '1';
    this.showCreationWelcome = this.route.snapshot.queryParamMap.get('created') === '1';

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    const quizId = Number(id);
    this.loadQuiz(quizId);
  }

  ngOnDestroy(): void {
    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
    }
    if (this.quizTimerInterval) {
      clearInterval(this.quizTimerInterval);
    }
    if (this.noticeTimeout) {
      clearTimeout(this.noticeTimeout);
    }
    if (this.highlightTimeout) {
      clearTimeout(this.highlightTimeout);
    }
  }

  private loadQuiz(quizId: number) {
    this.quizService.getQuizById(quizId).subscribe({
      next: (quiz) => {
        this.quiz = quiz;
        this.quizService.getQuizQuestions(quizId).subscribe({
          next: (questions) => {
            this.quiz.questions = questions;
            this.quiz.questions.forEach((question) => this.loadQuestionAnswers(question));

            if (this.isAdminQuizContext && this.autoOpenEditMode) {
              this.enableEditMode();

              if (this.showCreationWelcome) {
                this.showNotice(`"${quiz.title}" is ready. Add your questions and answers now.`, 'success');
              }
            }

            if (!this.isAdminQuizContext) {
              if (this.studentId) {
                this.checkQuizStatus(quizId);
              } else {
                this.startQuizTimer();
              }
            }
          },
          error: (err) => {
            console.error('Failed to load quiz questions', err);
            this.showNotice('Unable to load quiz questions right now.', 'error');
          }
        });
      },
      error: (err) => {
        console.error('Failed to load quiz', err);
        this.showNotice('Unable to load this quiz right now.', 'error');
      }
    });
  }

  private loadQuestionAnswers(question: Question) {
    this.showCorrectAnswerMap[question.id] = false;

    this.quizService.getAnswersByQuestion(question.id).subscribe({
      next: (answers) => {
        question.answers = answers;
        const correctAnswers = answers.filter((answer) => answer.correct);
        if (correctAnswers.length > 0) {
          this.correctAnswersMap[question.id] = correctAnswers;
        }
      },
      error: (err) => {
        console.error('Failed to load answers', err);
      }
    });
  }

  checkQuizStatus(quizId: number) {
    if (!this.studentId) {
      this.startQuizTimer();
      return;
    }

    this.quizService.getQuizStatus(quizId, this.studentId).subscribe({
      next: (status) => {
        if (!status) {
           this.startQuizTimer();
           return;
        }
        try {
          this.attemptCount = status.totalAttempts || 0;
          const hasPassed = status.passed || false;
          const attempts = Array.isArray(status.attempts) ? status.attempts : [];
          const bestScore = attempts.length
            ? Math.max(...attempts.map((attempt: any) => Number(attempt?.score ?? 0)))
            : null;

          if (bestScore !== null) {
            this.quizScore = bestScore;
          }

          if (hasPassed) {
            this.stopQuizTimer();
            this.submitDisabled = true;
            this.passedQuiz = true;
            this.setCompletionState(bestScore ?? this.quiz.passingScore, true);
            this.quiz.questions.forEach((question) => {
              this.showCorrectAnswerMap[question.id] = true;
            });
            return;
          }

          const activeCooldown = this.getActiveCooldownRemaining(attempts);
          if (activeCooldown > 0) {
            this.startCooldown(activeCooldown);
            return;
          }

          this.submitDisabled = false;
          this.cooldownRemaining = 0;
          this.startQuizTimer();
        } catch (e) {
          console.error("Status parsing error", e);
          this.startQuizTimer();
        }
      },
      error: (err) => {
        console.error('Failed to load quiz status', err);
        this.startQuizTimer();
      }
    });
  }

  startCooldown(seconds: number) {
    this.submitDisabled = true;
    this.cooldownRemaining = seconds;

    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
    }

    this.cooldownInterval = setInterval(() => {
      this.cooldownRemaining--;

      if (this.cooldownRemaining <= 0) {
        if (this.cooldownInterval) {
          clearInterval(this.cooldownInterval);
        }
        this.submitDisabled = false;
        this.attemptCount = 0;

        if (this.userRole === 'STUDENT' && !this.passedQuiz) {
          this.startQuizTimer();
        }
      }
    }, 1000);
  }

  selectAnswer(questionId: number, answerId: number) {
    this.selectedAnswers[questionId] = answerId;
  }

  toggleShowCorrectAnswer(questionId: number) {
    this.showCorrectAnswerMap[questionId] = !this.showCorrectAnswerMap[questionId];
  }

  get answeredQuestionCount(): number {
    return Object.keys(this.selectedAnswers).length;
  }

  get totalQuestionCount(): number {
    return this.quiz.questions?.length ?? 0;
  }

  submitQuiz(options?: { autoSubmitted?: boolean; forceFailed?: boolean }) {
    if (!this.studentId) {
      this.showNotice('Student session is invalid. Please log out and log in again before submitting the quiz.', 'error');
      return;
    }

    const autoSubmitted = options?.autoSubmitted ?? false;
    const forceFailed = options?.forceFailed ?? false;

    this.stopQuizTimer();
    this.isAutoSubmittingQuiz = autoSubmitted;

    const payload = {
      quizId: this.quiz.id,
      studentId: this.studentId,
      answers: forceFailed ? {} : this.selectedAnswers
    };

    this.quizService.submitQuiz(payload).subscribe({
      next: (res) => {
        const score = forceFailed ? 0 : Number(res?.score ?? 0);
        const passed = forceFailed ? false : Boolean(res?.passed ?? score >= this.quiz.passingScore);

        this.quizScore = score;
        this.attemptCount += 1;

        if (passed) {
          this.submitDisabled = true;
          this.passedQuiz = true;
          this.isAutoSubmittingQuiz = false;
          this.setCompletionState(score);
          this.quiz.questions.forEach((question) => {
            this.showCorrectAnswerMap[question.id] = true;
          });
          this.showNotice(
            autoSubmitted
              ? `Time closed and your answers were submitted. Excellent work, you still passed "${this.quiz.title}" with ${score}%.`
              : `Excellent work. You passed "${this.quiz.title}" with ${score}% and completed the quiz successfully.`,
            'success'
          );
          return;
        }

        this.completionTitle = '';
        this.completionMessage = '';
        this.showNotice(
          forceFailed
            ? `The 30-second timer ended. This attempt is marked as failed, and you scored 0%.`
            : autoSubmitted
              ? `Time is up. Your quiz was submitted automatically with ${score}%. Reach ${this.quiz.passingScore}% or more to pass.`
              : `Quiz submitted. You scored ${score}%. Reach ${this.quiz.passingScore}% or more to pass.`,
          'info'
        );

        if (this.attemptCount >= 3) {
          this.startCooldown(this.retryCooldownSeconds);
        } else if (this.userRole === 'STUDENT') {
          this.startQuizTimer();
        }

        this.isAutoSubmittingQuiz = false;
      },
      error: (err) => {
        this.isAutoSubmittingQuiz = false;
        console.error('Quiz submission failed', err);
        const backendMessage = err?.error?.message || err?.error?.error || err?.message;
        this.showNotice(
          backendMessage || 'Unable to submit the quiz right now. Please verify the Quiz backend is running and log in again.',
          'error'
        );

        if (this.userRole === 'STUDENT' && !this.passedQuiz && !this.submitDisabled) {
          this.startQuizTimer();
        }
      }
    });
  }

  generateCertificate() {
    if (!this.studentId) {
      this.showNotice('Student session is missing. Please log in again before generating the certificate.', 'error');
      return;
    }

    if (!this.passedQuiz) {
      this.showNotice('Pass the quiz first, then generate your certificate.', 'info');
      return;
    }

    const studentEmail = this.resolveStudentEmail();
    if (!studentEmail) {
      this.showNotice('Your account email is missing. Please log in again before generating the certificate.', 'error');
      return;
    }

    this.isGeneratingCertificate = true;

    this.quizService.generateCourseCertificate({
      quizId: this.quiz.id,
      studentId: this.studentId,
      studentName: this.resolveStudentName(),
      studentEmail
    }).subscribe({
      next: (response) => {
        this.isGeneratingCertificate = false;

        if (!response.body) {
          this.showNotice('The certificate was generated, but the file was empty. Please try again.', 'error');
          return;
        }

        const fileName = this.extractDownloadFileName(response.headers.get('content-disposition'))
          || this.buildCertificateFileName();

        this.downloadCertificateFile(response.body, fileName);
        this.showNotice('Your course certificate is ready, downloaded, and sent to your email with a QR code.', 'success');
      },
      error: (err) => {
        const blobError = err?.error;

        if (blobError instanceof Blob) {
          blobError.text().then((message) => {
            this.isGeneratingCertificate = false;
            this.showNotice(this.extractServerMessage(message), 'error');
          }).catch(() => {
            this.isGeneratingCertificate = false;
            this.showNotice('Unable to generate the certificate right now. Please try again.', 'error');
          });
          return;
        }

        this.isGeneratingCertificate = false;
        const backendMessage = err?.error?.message || err?.error?.error || err?.message;
        this.showNotice(backendMessage || 'Unable to generate the certificate right now. Please try again.', 'error');
      }
    });
  }

  deleteQuiz() {
    if (!this.quiz?.id) {
      return;
    }

    if (!this.deleteConfirmationRequired) {
      this.deleteConfirmationRequired = true;
      this.showNotice(`Delete mode enabled for "${this.quiz.title}". Click Delete Quiz once more to remove it permanently.`, 'error');
      return;
    }

    this.quizService.deleteQuiz(this.quiz.id).subscribe({
      next: () => {
        this.deleteConfirmationRequired = false;
        this.showNotice('Quiz deleted successfully.', 'success');
        this.router.navigate([this.isAdminQuizContext ? '/admin/quizzes' : '/courses']);
      },
      error: (err) => {
        this.deleteConfirmationRequired = false;
        console.error('Quiz deletion failed', err);
        const backendMessage = err?.error?.message || err?.error?.error || err?.message;
        this.showNotice(backendMessage || 'Unable to delete this quiz right now.', 'error');
      }
    });
  }

  enableEditMode() {
    this.isEditMode = true;
    this.deleteConfirmationRequired = false;
    this.editedQuiz = JSON.parse(JSON.stringify(this.quiz));
    this.showNotice('Editing mode is active. Update text or answer status freely, then save your work.', 'info');
  }

  saveQuiz() {
    if (!this.editedQuiz) {
      return;
    }

    this.quizService.updateQuiz(this.quiz.id, this.editedQuiz).subscribe({
      next: (updated) => {
        this.quiz = updated;
        this.isEditMode = false;
        this.deleteConfirmationRequired = false;
        this.showNotice('Quiz updated successfully.', 'success');
      },
      error: (err) => {
        console.error('Quiz update failed', err);
        this.showNotice('Unable to save the quiz right now.', 'error');
      }
    });
  }

  saveQuestion(question: Question) {
    this.quizService.updateQuestion(question.id, question).subscribe({
      next: () => {
        if (question.answers && question.answers.length > 0) {
          const answerRequests = question.answers.map(a => this.quizService.updateAnswer(a.id, a));
          forkJoin(answerRequests).subscribe({
            next: () => this.showNotice('Question et réponses enregistrées avec succès.', 'success'),
            error: (err) => {
              console.error('Answer update failed', err);
              this.showNotice('La question est sauvée, mais certaines réponses n\'ont pas pu être mises à jour.', 'error');
            }
          });
        } else {
          this.showNotice('Question enregistrée avec succès.', 'success');
        }
      },
      error: (err) => {
        console.error('Question update failed', err);
        this.showNotice('Impossible de sauvegarder cette question pour le moment.', 'error');
      }
    });
  }

  cancelEdit() {
    this.isEditMode = false;
    this.deleteConfirmationRequired = false;
    this.showNotice('Editing closed. Your last unsaved changes were discarded.', 'info');
  }

  saveAnswer(answer: Answer, silent: boolean = false) {
    if (!answer.id) {
      return;
    }

    this.quizService.updateAnswer(answer.id, answer).subscribe({
      next: (updated) => {
        answer.correct = updated.correct;
        answer.text = updated.text;
        if (!silent) {
          this.showNotice(
            updated.correct
              ? `"${updated.text}" is now marked as correct.`
              : `"${updated.text}" is now marked as wrong.`,
            'success'
          );
        }
      },
      error: (err) => {
        console.error('Answer update failed', err);
        if (!silent) this.showNotice('Unable to update this answer right now.', 'error');
      }
    });
  }

  setEditedAnswerCorrectness(answer: Answer, isCorrect: boolean) {
    answer.correct = isCorrect;
    this.saveAnswer(answer, true);
  }

  autoSaveQuestion(question: Question) {
    if (!question.text || question.text.length < 5) return;
    this.quizService.updateQuestion(question.id, question).subscribe();
  }

  autoSaveAnswer(answer: Answer) {
    if (!answer.text || answer.text.trim().length === 0) return;
    this.saveAnswer(answer, true);
  }

  deleteAnswer(questionIndex: number, answerIndex: number) {
    const answer = this.editedQuiz.questions[questionIndex].answers[answerIndex];

    if (!answer.id) {
      return;
    }

    this.quizService.deleteAnswer(answer.id).subscribe({
      next: () => {
        this.editedQuiz.questions[questionIndex].answers.splice(answerIndex, 1);
        this.showNotice('Answer removed from the quiz.', 'success');
      },
      error: (err) => {
        console.error('Answer delete failed', err);
        this.showNotice('Unable to delete this answer right now.', 'error');
      }
    });
  }

  addQuestion() {
    const newQuestion: Question = {
      id: 0,
      text: 'New Question',
      answers: []
    };

    this.quizService.addQuestion(this.quiz.id, newQuestion).subscribe({
      next: (saved) => {
        if (!this.editedQuiz.questions) {
          this.editedQuiz.questions = [];
        }

        this.editedQuiz.questions.push({
          ...saved,
          answers: saved.answers || []
        });
        this.showNotice('A new question was added. Continue building the quiz freely.', 'success');
      },
      error: (err) => {
        console.error('Add question failed', err);
        this.showNotice('Unable to add the new question right now.', 'error');
      }
    });
  }

  deleteQuestion(questionId: number, index: number) {
    if (!questionId) {
      return;
    }

    this.quizService.deleteQuestion(questionId).subscribe({
      next: () => {
        this.editedQuiz.questions.splice(index, 1);
        this.showNotice('Question deleted successfully.', 'success');
      },
      error: (err) => {
        console.error('Delete question failed', err);
        this.showNotice('Unable to delete this question right now.', 'error');
      }
    });
  }

  addAnswer(question: Question) {
    const newAnswer: Answer = {
      id: 0,
      text: 'New Answer',
      correct: false
    };

    this.quizService.addAnswer(question.id, newAnswer).subscribe({
      next: (saved) => {
        question.answers.push(saved);
        this.showNotice('New answer added. Mark it as correct or wrong, then save it.', 'success');
      },
      error: (err) => {
        console.error('Add answer failed', err);
        this.showNotice('Unable to add the new answer right now.', 'error');
      }
    });
  }

  private setCompletionState(score: number, alreadyCompleted = false) {
    this.passedQuiz = true;
    this.quizScore = score;
    this.completionTitle = alreadyCompleted ? 'Quiz Completed' : 'Congratulations!';
    this.completionMessage = alreadyCompleted
      ? `You already completed this quiz successfully with a best score of ${score}%. Great work.`
      : `You completed this quiz successfully with ${score}%. Keep going, you are ready for the next challenge.`;
  }

  private resolveStudentId(): string {
    const directValue = localStorage.getItem('USER_ID') || localStorage.getItem('id');
    if (directValue) {
      return directValue;
    }

    try {
      const rawUser = localStorage.getItem('user');
      if (!rawUser) {
        return '';
      }

      const parsedUser = JSON.parse(rawUser);
      return parsedUser?.id ? String(parsedUser.id) : '';
    } catch (error) {
      console.warn('Unable to read stored user for quiz submission', error);
      return '';
    }
  }

  private resolveStudentName(): string {
    try {
      const rawUser = localStorage.getItem('user');
      if (!rawUser) {
        return '';
      }

      const parsedUser = JSON.parse(rawUser);
      const fullName = [parsedUser?.name, parsedUser?.lastName]
        .filter((value: unknown) => typeof value === 'string' && value.trim().length > 0)
        .join(' ')
        .trim();

      return fullName;
    } catch (error) {
      console.warn('Unable to read stored user for certificate generation', error);
      return '';
    }
  }

  private resolveStudentEmail(): string {
    try {
      const rawUser = localStorage.getItem('user');
      if (!rawUser) {
        return '';
      }

      const parsedUser = JSON.parse(rawUser);
      return typeof parsedUser?.email === 'string' ? parsedUser.email.trim() : '';
    } catch (error) {
      console.warn('Unable to read stored user email for certificate generation', error);
      return '';
    }
  }

  private isValidUuid(value: unknown): value is string {
    return typeof value === 'string' && value.trim().length > 0;
  }

  private downloadCertificateFile(blob: Blob, fileName: string) {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  private extractDownloadFileName(contentDisposition: string | null): string | null {
    if (!contentDisposition) {
      return null;
    }

    const match = contentDisposition.match(/filename="?([^"]+)"?/i);
    return match?.[1] ?? null;
  }

  private buildCertificateFileName(): string {
    const normalizedTitle = (this.quiz.title || 'course-certificate')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');

    return `${normalizedTitle || 'course-certificate'}.pdf`;
  }

  private extractServerMessage(rawMessage: string): string {
    if (!rawMessage) {
      return 'Unable to generate the certificate right now. Please try again.';
    }

    try {
      const parsed = JSON.parse(rawMessage);
      return parsed?.message || parsed?.error || rawMessage;
    } catch {
      return rawMessage;
    }
  }

  private startQuizTimer() {
    if (this.isAdminQuizContext || this.isEditMode || this.passedQuiz) {
      return;
    }

    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
      this.cooldownInterval = null;
    }

    this.submitDisabled = false;
    this.cooldownRemaining = 0;
    this.remainingSeconds = this.quizTimeLimitSeconds;
    this.updateTimerDisplay();

    if (this.quizTimerInterval) {
      clearInterval(this.quizTimerInterval);
    }

    this.quizTimerInterval = setInterval(() => {
      this.remainingSeconds--;
      this.updateTimerDisplay();
      this.cdr.detectChanges();

      if (this.remainingSeconds === 10) {
        this.showNotice('Ten seconds left. Finish fast before the attempt is marked as failed.', 'info');
      }

      if (this.remainingSeconds <= 5 && this.remainingSeconds > 0) {
        this.showNotice('Final five seconds. If the timer reaches zero, this attempt fails automatically.', 'error');
      }

      if (this.remainingSeconds <= 0) {
        this.handleTimerExpired();
      }
    }, 1000);
  }

  private stopQuizTimer() {
    if (this.quizTimerInterval) {
      clearInterval(this.quizTimerInterval);
      this.quizTimerInterval = null;
    }
  }

  private updateTimerDisplay() {
    const safeSeconds = Math.max(this.remainingSeconds, 0);
    const minutes = Math.floor(safeSeconds / 60);
    const seconds = safeSeconds % 60;

    this.timerDisplay = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

    if (safeSeconds <= 5) {
      this.timerTone = 'critical';
      return;
    }

    if (safeSeconds <= 10) {
      this.timerTone = 'warning';
      return;
    }

    this.timerTone = 'calm';
  }

  private handleTimerExpired() {
    this.stopQuizTimer();
    this.remainingSeconds = 0;
    this.updateTimerDisplay();
    this.showNotice('Time is up. This attempt is now marked as failed.', 'error');
    this.submitQuiz({ autoSubmitted: true, forceFailed: true });
  }

  private getActiveCooldownRemaining(attempts: any[]): number {
    if (!attempts.length || attempts.length < 3) {
      return 0;
    }

    const latestAttempt = attempts[attempts.length - 1];
    const submittedAt = latestAttempt?.submittedAt;
    if (!submittedAt) {
      return 0;
    }

    const submittedAtMs = new Date(submittedAt).getTime();
    if (Number.isNaN(submittedAtMs)) {
      return 0;
    }

    const elapsedSeconds = Math.floor((Date.now() - submittedAtMs) / 1000);
    return Math.max(this.retryCooldownSeconds - elapsedSeconds, 0);
  }

  generateAiRecommendation() {
    if (!this.quiz?.id) {
      return;
    }

    this.isGeneratingAiRecommendation = true;

    this.quizService.getQuizRecommendation(this.quiz.id).subscribe({
      next: (recommendation) => {
        this.aiRecommendation = recommendation;
        this.isGeneratingAiRecommendation = false;
        this.showNotice(
          `Gemini prepared a new question draft based on ${recommendation.courseTitle}. Review it, then add it to the quiz when you are ready.`,
          'success'
        );
      },
      error: (err) => {
        this.isGeneratingAiRecommendation = false;
        console.error('Gemini recommendation failed', err);
        const backendMessage = err?.error?.message || err?.error?.error || err?.message;
        this.showNotice(backendMessage || 'Gemini could not prepare a recommendation right now.', 'error');
      }
    });
  }

  applyAiRecommendation() {
    if (!this.aiRecommendation || !this.quiz?.id) {
      return;
    }

    this.isApplyingAiRecommendation = true;

    this.quizService.addQuestion(this.quiz.id, {
      text: this.aiRecommendation.questionText,
      answers: []
    }).pipe(
      switchMap((savedQuestion) => {
        if (!savedQuestion.id) {
          throw new Error('The recommended question was created without an id.');
        }

        return forkJoin(
          this.aiRecommendation!.answers.map((answer) =>
            this.quizService.addAnswer(savedQuestion.id, {
              text: answer.text,
              correct: answer.correct
            })
          )
        ).pipe(
          map((savedAnswers) => ({
            savedQuestion,
            savedAnswers
          }))
        );
      })
    ).subscribe({
      next: ({ savedQuestion, savedAnswers }) => {
        const persistedQuestion: Question = {
          ...savedQuestion,
          answers: savedAnswers
        };

        this.quiz.questions = [...(this.quiz.questions || []), persistedQuestion];
        this.editedQuiz.questions = [...(this.editedQuiz.questions || []), {
          ...persistedQuestion,
          answers: [...savedAnswers]
        }];

        this.aiRecommendation = null;
        this.isApplyingAiRecommendation = false;
        this.highlightGeneratedQuestion(savedQuestion.id);
        this.showNotice('Gemini generated a fresh question and answers for this quiz. You can fine-tune them right away in the builder below.', 'success');
      },
      error: (err) => {
        this.isApplyingAiRecommendation = false;
        console.error('Applying Gemini recommendation failed', err);
        const backendMessage = err?.error?.message || err?.error?.error || err?.message;
        this.showNotice(backendMessage || 'Unable to add the Gemini recommendation to this quiz right now.', 'error');
      }
    });
  }

  private highlightGeneratedQuestion(questionId: number) {
    this.highlightedQuestionId = questionId;

    window.setTimeout(() => {
      const questionCard = document.getElementById(`question-card-${questionId}`);
      questionCard?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 120);

    if (this.highlightTimeout) {
      clearTimeout(this.highlightTimeout);
    }

    this.highlightTimeout = setTimeout(() => {
      this.highlightedQuestionId = null;
    }, 5000);
  }

  private showNotice(message: string, tone: 'success' | 'error' | 'info') {
    this.noticeMessage = message;
    this.noticeTone = tone;

    if (this.noticeTimeout) {
      clearTimeout(this.noticeTimeout);
    }

    this.noticeTimeout = setTimeout(() => {
      this.noticeMessage = '';
    }, 4200);
  }
}
