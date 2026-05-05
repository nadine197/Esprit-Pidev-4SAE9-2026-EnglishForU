import { Component, HostListener, OnInit } from '@angular/core';
import { AppointmentService } from '../../../services/appointment.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-take-test',
  templateUrl: './take-test.component.html'
})
export class TakeTestComponent implements OnInit {
  activeSession: any;
  currentQuestionIndex = 0;
  score = 0;
  testFinished = false;
  finalLevel = ""; // Propriété de classe utilisée pour les tests
  cheatCount = 0; 

  questions = [
    {
      q: "Which sentence is grammatically correct?",
      options: ["He don't like coffee", "He doesn't likes coffee", "He doesn't like coffee", "He not like coffee"],
      answer: "He doesn't like coffee"
    },
    {
      q: "If I _______ rich, I would travel the world.",
      options: ["am", "was", "were", "be"],
      answer: "were"
    },
    {
      q: "I have been living here _______ 2010.",
      options: ["for", "since", "during", "ago"],
      answer: "since"
    },
    {
      q: "By the time you arrive, we _______ the meeting.",
      options: ["will finish", "will have finished", "finish", "have finished"],
      answer: "will have finished"
    }
  ];

  constructor(private apptService: AppointmentService, private router: Router) {}

  ngOnInit() {
    const data = sessionStorage.getItem('active_session');
    if (!data) {
      this.router.navigate(['/book-test']);
      return;
    }
    this.activeSession = JSON.parse(data);
  }

  selectOption(selected: string) {
    // Vérification de la réponse
    if (selected === this.questions[this.currentQuestionIndex].answer) {
      this.score++;
    }

    // Passage à la question suivante ou calcul final
    if (this.currentQuestionIndex < this.questions.length - 1) {
      this.currentQuestionIndex++;
    } else {
      this.finishTest();
    }
  }

  finishTest() {
    this.testFinished = true;

    const totalQuestions = this.questions.length;
    const scoreFinal = `${this.score} / ${totalQuestions}`;
    
    // Calcul du ratio pour déterminer le niveau
    const ratio = (this.score / totalQuestions) * 100;

    // FIX : Utilisation de "this.finalLevel" au lieu d'une variable locale "let level"
    // Cela permet au test unitaire d'accéder à la valeur calculée
    if (ratio >= 80) {
      this.finalLevel = "C1 - Advanced";
    } else if (ratio >= 50) {
      this.finalLevel = "B2 - Intermediate";
    } else {
      this.finalLevel = "A1 - Beginner";
    }

    // Envoi des résultats au microservice via la Gateway
    if (this.activeSession && this.activeSession.id) {
      this.apptService.completeAppointment(
        this.activeSession.id, 
        this.finalLevel, 
        scoreFinal, 
        this.cheatCount 
      ).subscribe({
        next: () => {
          console.log("Test results and Proctoring data saved.");
        },
        error: (err) => {
          console.error("Error saving test results:", err);
          // On ne met pas d'alert ici pour ne pas bloquer les tests unitaires automatisés
        }
      });
    }
  }

  goHome() {
    sessionStorage.removeItem('active_session');
    this.router.navigate(['/']);
  }

  // Système anti-triche (Proctoring)
  @HostListener('window:blur', [])
  onWindowBlur() {
    if (!this.testFinished) {
      this.cheatCount++;
      // Note : l'alerte peut bloquer certains environnements de test, 
      // mais elle est conservée pour la logique métier réelle.
      alert(`WARNING: You have left the test page ${this.cheatCount} time(s). This activity is logged for the administrator.`);
    }
  }
}