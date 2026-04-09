import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StudentHomeComponent } from './student-home/student-home';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    StudentHomeComponent
  ],
  imports: [
    CommonModule,   // Fixes ngClass, ngIf, ngFor
    SharedModule,   // Fixes <app-navbar> and <app-footer>
    RouterModule    // Fixes routerLink
  ]
})
export class StudentModule { }