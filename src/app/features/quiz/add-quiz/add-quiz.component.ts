import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService, Quiz, Question, Answer } from 'src/app/services/quiz.service';

@Component({
  selector: 'app-add-quiz',
  templateUrl: './add-quiz.component.html',
  styleUrls: ['./add-quiz.component.css']
})

export class AddQuizComponent implements OnInit{
  quiz: Quiz = { id: 0, title: '', passingScore: 70, questions: [] }; 
  createdQuiz!: Quiz; 
  newQuestionText: string = '';
  newAnswerText: string = '';
  currentQuestionIndex: number = -1; 

  constructor(  private route: ActivatedRoute,
      private quizService: QuizService,
      private router: Router) {

      }
courseId!: number;

ngOnInit() {
  this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
  console.log('courseId =', this.courseId);
}
createQuiz() {
  if (!this.quiz.title.trim()) return;

  const payload = { title: this.quiz.title, passingScore: 70, questions: [], courseId: this.courseId };

  this.quizService.addQuiz(this.courseId, payload).subscribe({
    next: (quiz) => {
      console.log('Quiz créé:', quiz);
      this.createdQuiz = quiz;
      this.quiz = quiz;
    },
    error: (err) => console.error(err)
  });
}


addQuestion() {
  const newQuestion: any = {
    text: this.newQuestionText,
    answers: [] 
  };

  this.quizService.addQuestion(this.quiz.id, newQuestion).subscribe({
    next: (saved) => {
      
      this.quiz.questions = [];
      this.quiz.questions.push({
        ...saved,
        answers: saved.answers || []
      });
    },
    error: (err) => console.error(err)
  });
}




addAnswer(question: Question) {
  const newAnswer: any = {
    text: this.newAnswerText,
    correct: false
  };

  this.quizService.addAnswer(question.id, newAnswer).subscribe({
    next: (saved) => {
      this.newAnswerText = ''; 
      question.answers.push(saved);
    },
    error: (err) => console.error(err)
  });
}
  // 4️⃣ Sélectionner une question pour ajouter des réponses
  selectQuestion(index: number) {
    if (index >= 0 && index < this.quiz.questions.length) {
      this.currentQuestionIndex = index;
    }
  }
}