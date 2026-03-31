import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { TutorLayoutComponent } from './layout/tutor-layout';
import { TutorDashboardComponent } from './dashboard/tutor-dashboard';

@NgModule({
  declarations: [
    TutorLayoutComponent,
    TutorDashboardComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule
  ]
})
export class TutorModule { }
