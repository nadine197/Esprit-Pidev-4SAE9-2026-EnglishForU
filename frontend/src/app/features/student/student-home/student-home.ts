import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-student-home',
  templateUrl: './student-home.html'
})
export class StudentHomeComponent implements OnInit {
  currentUser: any;
  features = [
    { title: 'My Progress', desc: 'Check your scores and levels.', icon: '📈' },
    { title: 'Quizzes', desc: 'Pending assessments for you.', icon: '📝' },
    { title: 'Live Class', desc: 'Join your tutor online.', icon: '🎥' },
    { title: 'Certificates', desc: 'Download your diplomas.', icon: '🏆' }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
  }
}