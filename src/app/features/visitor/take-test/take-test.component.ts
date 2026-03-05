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
  finalLevel = "";
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
    if (selected === this.questions[this.currentQuestionIndex].answer) {
      this.score++;
    }

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
  
  const ratio = (this.score / totalQuestions) * 100;
  let level = "A1 - Beginner"; 
  if (ratio >= 80) {
    level = "C1 - Advanced";
  } else if (ratio >= 50) {
    level = "B2 - Intermediate";
  }

  
  this.apptService.completeAppointment(
    this.activeSession.id, 
    level, 
    scoreFinal, 
    this.cheatCount 
  ).subscribe({
    next: () => {
      console.log("Test results and Proctoring data saved.");
    },
    error: (err) => {
      console.error("Error saving test results:", err);
      alert("An error occurred while saving your results.");
    }
  });
}
  goHome() {
    sessionStorage.removeItem('active_session');
    this.router.navigate(['/']);
  }

  @HostListener('window:blur', [])
  onWindowBlur() {
    if (!this.testFinished) {
      this.cheatCount++;
      alert(`WARNING: You have left the test page ${this.cheatCount} time(s). This activity is logged for the administrator.`);
    }
  }
}