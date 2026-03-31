import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { StudentHomeComponent } from './student-home/student-home';
import { StudentLayoutComponent } from './student-layout/student-layout';
import { ClubListComponent } from './club-list/club-list';
import { ClubDetailComponent } from './club-detail/club-detail';
import { ClubFormComponent } from './club-form/club-form';
import { EventListComponent } from './event-list/event-list';
import { EventDetailComponent } from './event-detail/event-detail';
import { EventFormComponent } from './event-form/event-form';
import { MyTicketsComponent } from './my-tickets/my-tickets';
import { EventFeedbackComponent } from './event-feedback/event-feedback';
import { ClubStatsComponent } from './club-stats/club-stats';

@NgModule({
  declarations: [
    StudentHomeComponent,
    StudentLayoutComponent,
    ClubListComponent,
    ClubDetailComponent,
    ClubFormComponent,
    EventListComponent,
    EventDetailComponent,
    EventFormComponent,
    MyTicketsComponent,
    EventFeedbackComponent,
    ClubStatsComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class StudentModule { }

