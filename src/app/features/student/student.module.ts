import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StudentHomeComponent } from './student-home/student-home';
import { SharedModule } from '../shared/shared.module';
<<<<<<< HEAD

@NgModule({
  declarations: [
    StudentHomeComponent
=======
import { StudentQuizzesComponent } from './student-quizzes/student-quizzes';

@NgModule({
  declarations: [
    StudentHomeComponent,
    StudentQuizzesComponent
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  ],
  imports: [
    CommonModule,   // Fixes ngClass, ngIf, ngFor
    SharedModule,   // Fixes <app-navbar> and <app-footer>
    RouterModule    // Fixes routerLink
  ]
})
<<<<<<< HEAD
export class StudentModule { }
=======
export class StudentModule { }
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
