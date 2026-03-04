import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CertificateService } from 'src/app/services/certificate.service';
import { QuizService, Quiz, Question, Answer } from 'src/app/services/quiz.service';

@Component({
  selector: 'app-quiz-details',
  templateUrl: './quiz-details.component.html'
})
export class QuizDetailsComponent implements OnInit {

  quiz: Quiz = { id: 0, title: '', passingScore: 0, questions: [] };
  selectedAnswers: { [questionId: number]: number } = {};
  showCorrectAnswerMap: { [questionId: number]: boolean } = {};
  correctAnswersMap: { [questionId: number]: Answer[] } = {};
  userRole : string='' ;
  passedQuiz: boolean = false; 
  quizScore!: number;
  attemptCount: number = 0;
  submitDisabled: boolean = false;
  cooldownRemaining: number = 0;
private cooldownInterval: any;
isEditMode = false;
editedQuiz!: Quiz;
  private readonly studentId = "19650b98-80c8-49ba-af6b-7bd647cf9ebd";
showCertPopup = false;
certUserName = '';
certUserEmail = '';
certificateId!: number;
courseId!: number;
isSending = false; // 🔥 loading
  constructor(
    private route: ActivatedRoute,
    private quizService: QuizService,
    private router: Router,
      private certificateService: CertificateService

  ) {}

ngOnInit(): void {

  // 🔹 Récupérer le rôle
  const storedRole = localStorage.getItem('ROLE');
  this.userRole = storedRole ? storedRole.replace(/"/g, '') : '';

  const id = this.route.snapshot.paramMap.get('id');
  if (!id) return;

  const quizId = +id;

  // 1️⃣ Récupérer le quiz
  this.quizService.getQuizById(quizId).subscribe(data => {
    this.quiz = data;

    // 2️⃣ Récupérer les questions
    this.quizService.getQuizQuestions(quizId).subscribe(questions => {
      this.quiz.questions = questions;

      // Charger les réponses
      this.quiz.questions.forEach(q => {
        this.showCorrectAnswerMap[q.id] = false;

        this.quizService.getAnswersByQuestion(q.id).subscribe(answers => {
          q.answers = answers;

        const correctAnswers = answers.filter(a => a.correct);

        if (correctAnswers.length > 0) {
          this.correctAnswersMap[q.id] = correctAnswers;
        }
        });
      });

      // ✅ 3️⃣ Vérifier le statut APRÈS chargement complet
      this.checkQuizStatus(quizId);

    });
  });
}
checkQuizStatus(quizId: number) {

  this.quizService.getQuizStatus(quizId, this.studentId)
    .subscribe(status => {

      this.attemptCount = status.totalAttempts;
      const hasPassed = status.passed;

      // 🎯 Cas : déjà réussi
      if (hasPassed) {
        this.submitDisabled = true;
        this.passedQuiz = true;

        // afficher les bonnes réponses
        this.quiz.questions.forEach(q => {
          this.showCorrectAnswerMap[q.id] = true;
        });

        return;
      }

      // 🎯 Cas : 3 échecs
      if (this.attemptCount >= 3) {
        this.startCooldown(30);
      }

    });
}
startCooldown(seconds: number) {
  this.submitDisabled = true;
  this.cooldownRemaining = seconds;

  this.cooldownInterval = setInterval(() => {
    this.cooldownRemaining--;

    if (this.cooldownRemaining <= 0) {
      clearInterval(this.cooldownInterval);
      this.submitDisabled = false;
      this.attemptCount = 0; 
    }
  }, 1000);
}
  selectAnswer(questionId: number, answerId: number) {
    this.selectedAnswers[questionId] = answerId;
  }

  toggleShowCorrectAnswer(questionId: number) {
    this.showCorrectAnswerMap[questionId] = !this.showCorrectAnswerMap[questionId];
  }

  submitQuiz() {
    const payload = {
      quizId: this.quiz?.id,
      studentId: this.studentId,
      answers: this.selectedAnswers
    };

    this.quizService.submitQuiz(payload).subscribe(res => {
      if (res.score >= 70){
      this.passedQuiz= true;
      }
      alert("Quiz submitted! Score: " + res.score);
      localStorage.setItem('score',res.score);
    });
  }
generateCertificate() {
  this.showCertPopup = true;
}
confirmGenerateCertificate() {

  if (!this.certUserName.trim() || !this.certUserEmail.trim()) {
    alert('Please fill all fields');
    return;
  }

  this.isSending = true;

  // ⚡ Payload pour créer le certificat directement
  const payload = {
    courseId: Number(localStorage.getItem('courseId')),      // id du cours actuel
    studentId:this.studentId,   // UUID de l'étudiant
    userName: this.certUserName,  // nom saisi pour le certificat
    userEmail: this.certUserEmail,// email de l'étudiant
    finalScore: this.quizScore  // score obtenu au quiz
  };

  // Appel au service pour générer le PDF + envoyer l'email
  this.certificateService.generateAndSend(payload).subscribe({
    next: (blob) => {
      // 📥 Téléchargement automatique du PDF
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'certificate.pdf';
      a.click();
      window.URL.revokeObjectURL(url);

      this.isSending = false;
      this.showCertPopup = false;

      // ✅ Message de succès
      this.showToast('Certificate sent successfully 🎉');
    },
    error: (err) => {
      console.error(err); // pour debug
      this.isSending = false;
      this.showToast('Error sending certificate ❌');
    }
  });
}
toastMessage = '';
showToastFlag = false;

showToast(msg: string) {
  this.toastMessage = msg;
  this.showToastFlag = true;

  setTimeout(() => {
    this.showToastFlag = false;
  }, 3000);
}
  deleteQuiz() {
    if (this.quiz?.id) {
      this.quizService.deleteQuiz(this.quiz.id).subscribe(() => {
        this.router.navigate(['/quiz']);
      });
    }
  }
  enableEditMode() {
  this.isEditMode = true;

  // Deep copy pour éviter modifier directement l’original
  this.editedQuiz = JSON.parse(JSON.stringify(this.quiz));
}
saveQuiz() {
  if (!this.editedQuiz) return;

  this.quizService.updateQuiz(this.quiz.id, this.editedQuiz)
    .subscribe(updated => {
      this.quiz = updated;
      this.isEditMode = false;

    });
}
saveQuestion(question: Question) {
  this.quizService.updateQuestion(question.id, question).subscribe(
    updated => {
      console.log("Question mise à jour :", updated);
    },
    err => console.error("Erreur lors de la mise à jour de la question", err)
  );
}
cancelEdit() {
  this.isEditMode = false;
}

saveAnswer(answer: Answer) {
  if (!answer.id) return;

  this.quizService.updateAnswer(answer.id, answer).subscribe({
    next: (updated) => {
      console.log("Answer updated ", updated);
    },
    error: (err) => {
      console.error("Erreur update answer ", err);
    }
  });
}

deleteAnswer(questionIndex: number, answerIndex: number) {
  const answer = this.editedQuiz.questions[questionIndex].answers[answerIndex];

  if (!answer.id) return;

  this.quizService.deleteAnswer(answer.id).subscribe({
    next: () => {
      // supprimer du tableau frontend
      this.editedQuiz.questions[questionIndex].answers.splice(answerIndex, 1);
      console.log("Answer deleted");
    },
    error: (err) => {
      console.error("Erreur delete answer", err);
    }
  });
}

addQuestion() {
  const newQuestion: any = {
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
    },
    error: (err) => console.error(err)
  });
}

deleteQuestion(questionId: number, index: number) {

  if (!questionId) return;

  this.quizService.deleteQuestion(questionId).subscribe({
    next: () => {
      this.editedQuiz.questions.splice(index, 1);
    },
    error: (err) => console.error(err)
  });
}


addAnswer(question: Question) {
  const newAnswer: any = {
    text: 'New Answer',
    correct: false
  };

  this.quizService.addAnswer(question.id, newAnswer).subscribe({
    next: (saved) => {
      question.answers.push(saved);
    },
    error: (err) => console.error(err)
  });
}
}