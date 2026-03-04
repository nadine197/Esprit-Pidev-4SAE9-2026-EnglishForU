import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService, Quiz } from 'src/app/services/quiz.service';

@Component({
  selector: 'app-quiz',
  templateUrl: './quiz.component.html'
})
export class QuizComponent implements OnInit {

  quizzes: Quiz[] = [];
  courseId!: number;

  constructor(
    private quizService: QuizService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

ngOnInit(): void {
  const id = this.route.snapshot.paramMap.get('id');
  if (id) {
    this.quizService.getQuizzesByCourse(+id).subscribe(data => {
      this.quizzes = data;
    });
  }
}

  startQuiz(id: number) {
    this.router.navigate(['/quizDetails', id]);
  }
}