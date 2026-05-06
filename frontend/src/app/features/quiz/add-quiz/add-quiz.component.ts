import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, Validators } from '@angular/forms';
import { Answer, Question, Quiz, QuizService } from 'src/app/services/quiz.service';

@Component({
  selector: 'app-add-quiz',
  templateUrl: './add-quiz.component.html',
  styleUrls: ['./add-quiz.component.css']
})
export class AddQuizComponent implements OnInit {
  quiz: Quiz = { id: 0, title: '', passingScore: 70, questions: [] };
  createdQuiz!: Quiz;

  titleControl = new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]);
  questionControl = new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(300)]);
  answerControl = new FormControl('', [Validators.required, Validators.minLength(1), Validators.maxLength(200)]);

  newAnswerIsCorrect = false;
  currentQuestionIndex = -1;
  courseId!: number;
  feedbackMessage = '';
  feedbackTone: 'success' | 'error' | 'info' = 'info';

  // ✅ AI
  aiLoading = false;
  aiRecommendation: any = null;

  private feedbackTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private route: ActivatedRoute,
    private quizService: QuizService,
    private router: Router
  ) {}

  ngOnInit() {
    this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));

    this.titleControl.valueChanges.subscribe(val => {
      this.quiz.title = val || '';
    });
  }

  get titreErreur(): string {
    if (this.titleControl.valid || !this.titleControl.touched) return '';
    if (this.titleControl.hasError('required')) return 'Le titre est obligatoire.';
    if (this.titleControl.hasError('minlength')) return 'Le titre doit contenir au moins 5 caractères.';
    if (this.titleControl.hasError('maxlength')) return 'Le titre ne peut pas dépasser 100 caractères.';
    return 'Titre invalide.';
  }

  get questionErreur(): string {
    if (this.questionControl.valid || !this.questionControl.touched) return '';
    if (this.questionControl.hasError('required')) return 'La question est obligatoire.';
    if (this.questionControl.hasError('minlength')) return 'La question doit contenir au moins 5 caractères.';
    if (this.questionControl.hasError('maxlength')) return 'La question ne peut pas dépasser 300 caractères.';
    return 'Question invalide.';
  }

  get reponseErreur(): string {
    if (this.answerControl.valid || !this.answerControl.touched) return '';
    if (this.answerControl.hasError('required')) return 'La réponse est obligatoire.';
    if (this.answerControl.hasError('maxlength')) return 'La réponse ne peut pas dépasser 200 caractères.';
    return 'Réponse invalide.';
  }

  createQuiz() {
    if (!localStorage.getItem('token')) {
      this.showFeedback('Session expirée. Veuillez vous reconnecter avant de créer un quiz.', 'error');
      return;
    }

    this.titleControl.markAsTouched();
    if (this.titleControl.invalid) return;

    const payload = {
      title: this.quiz.title,
      passingScore: 70,
      questions: [],
      courseId: this.courseId
    };

    this.quizService.addQuiz(this.courseId, payload).subscribe({
      next: (quiz) => {
        this.createdQuiz = quiz;
        // ✅ garantit que questions[] existe toujours
        this.quiz = { ...quiz, questions: quiz.questions || [] };
        this.currentQuestionIndex = -1;
        this.showFeedback('Quiz créé ! Ajoutez maintenant vos questions.', 'success');
      },
      error: (err) => {
        console.error(err);
        const backendMessage = err?.error?.message;
        if (err?.status === 401) {
          this.showFeedback('Non autorisé (401). Reconnectez-vous puis réessayez.', 'error');
          return;
        }
        if (err?.status === 403) {
          this.showFeedback('Accès refusé (403). Vérifiez que votre compte a le rôle ADMIN/SUPER_ADMIN/TUTOR.', 'error');
          return;
        }
        this.showFeedback(backendMessage || 'Unable to create the quiz right now. Please try again.', 'error');
      }
    });
  }

  addQuestion() {
    this.questionControl.markAsTouched();
    if (this.questionControl.invalid) return;

    const newQuestion = {
      text: this.questionControl.value!.trim(),
      answers: []
    };

    this.quizService.addQuestion(this.quiz.id, newQuestion).subscribe({
      next: (saved) => {
        this.questionControl.reset('');
        // ✅ sécurité : initialise questions[] si undefined
        if (!this.quiz.questions) this.quiz.questions = [];
        this.quiz.questions.push({ ...saved, answers: saved.answers || [] });
        this.currentQuestionIndex = this.quiz.questions.length - 1;
        this.answerControl.reset('');
        this.newAnswerIsCorrect = false;
        this.showFeedback('Question ajoutée. Ajoutez maintenant les réponses.', 'success');
      },
      error: (err) => {
        console.error(err);
        this.showFeedback('Impossible d\'ajouter la question. Réessayez.', 'error');
      }
    });
  }

  addAnswer(question: Question) {
    this.answerControl.markAsTouched();
    if (this.answerControl.invalid) return;

    const newAnswer = {
      text: this.answerControl.value!.trim(),
      correct: this.newAnswerIsCorrect
    };

    this.quizService.addAnswer(question.id, newAnswer).subscribe({
      next: (saved) => {
        this.answerControl.reset('');
        this.newAnswerIsCorrect = false;
        question.answers = [...(question.answers || []), saved];
        this.showFeedback(
          saved.correct ? 'Bonne réponse enregistrée.' : 'Mauvaise réponse enregistrée.',
          'success'
        );
      },
      error: (err) => {
        console.error(err);
        this.showFeedback('Impossible d\'enregistrer la réponse.', 'error');
      }
    });
  }

  setNewAnswerCorrectness(isCorrect: boolean) {
    this.newAnswerIsCorrect = isCorrect;
    this.showFeedback(
      isCorrect ? 'Cette réponse sera marquée comme correcte.' : 'Cette réponse sera marquée comme incorrecte.',
      'info'
    );
  }

  updateAnswerCorrectness(answer: Answer, isCorrect: boolean) {
    if (!answer.id) return;

    const updatedAnswer: Answer = { ...answer, correct: isCorrect };

    this.quizService.updateAnswer(answer.id, updatedAnswer).subscribe({
      next: (saved) => {
        answer.correct = saved.correct;
        answer.text = saved.text;
        this.showFeedback(
          saved.correct ? `"${saved.text}" marquée comme correcte.` : `"${saved.text}" marquée comme incorrecte.`,
          'success'
        );
      },
      error: (err) => {
        console.error('Failed to update answer correctness', err);
        this.showFeedback('Impossible de mettre à jour la réponse.', 'error');
      }
    });
  }

  selectQuestion(index: number) {
    if (index >= 0 && index < this.quiz.questions.length) {
      this.currentQuestionIndex = index;
      this.answerControl.reset('');
      this.newAnswerIsCorrect = false;
      this.showFeedback('Éditeur de réponses ouvert.', 'info');
    }
  }

  // ✅ Générer une recommandation AI
  generateAiRecommendation() {
    if (!this.quiz.id) {
      this.showFeedback('Créez d\'abord le quiz avant de générer une recommandation AI.', 'error');
      return;
    }
    this.aiLoading = true;
    this.aiRecommendation = null;
    this.quizService.getQuizRecommendation(this.quiz.id).subscribe({
      next: (rec) => {
        this.aiRecommendation = rec;
        this.aiLoading = false;
        this.showFeedback('Question AI générée avec succès !', 'success');
      },
      error: (err) => {
        console.error(err);
        this.aiLoading = false;
        const backendMessage = err?.error?.message;
        this.showFeedback(backendMessage || 'Impossible de générer une recommandation AI.', 'error');
      }
    });
  }

  // ✅ Appliquer la recommandation AI
  applyAiRecommendation() {
    if (!this.aiRecommendation) return;

    const newQuestion = {
      text: this.aiRecommendation.questionText,
      answers: this.aiRecommendation.answers || []
    };

    this.quizService.addQuestion(this.quiz.id, newQuestion).subscribe({
      next: (saved) => {
        if (!this.quiz.questions) this.quiz.questions = [];
        this.quiz.questions.push({ ...saved, answers: saved.answers || [] });
        this.aiRecommendation = null;
        this.showFeedback('Question AI ajoutée au quiz.', 'success');
      },
      error: (err) => {
        console.error(err);
        this.showFeedback('Impossible d\'ajouter la question AI.', 'error');
      }
    });
  }

  // ✅ Ignorer la recommandation AI
  dismissAiRecommendation() {
    this.aiRecommendation = null;
    this.showFeedback('Recommandation ignorée.', 'info');
  }

  // ✅ Terminer et sauvegarder le quiz
  finishQuiz() {
    if (!this.quiz.id) {
      this.showFeedback('Aucun quiz à sauvegarder.', 'error');
      return;
    }
    if (this.quiz.questions.length === 0) {
      this.showFeedback('Ajoutez au moins une question avant de terminer.', 'error');
      return;
    }
    this.router.navigate(['/admin/quizzes/details', this.quiz.id]);
  }

  private showFeedback(message: string, tone: 'success' | 'error' | 'info') {
    this.feedbackMessage = message;
    this.feedbackTone = tone;
    if (this.feedbackTimeout) clearTimeout(this.feedbackTimeout);
    this.feedbackTimeout = setTimeout(() => { this.feedbackMessage = ''; }, 4000);
  }
}
