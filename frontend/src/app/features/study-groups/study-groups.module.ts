import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StudyGroupsRoutingModule } from './study-groups-routing.module';
import { StudyGroupListComponent } from './pages/study-group-list/study-group-list.component';
import { StudyGroupFormComponent } from './pages/study-group-form/study-group-form.component';
import { StudyGroupDetailsComponent } from './pages/study-group-details/study-group-details.component';
import { StudyGroupCalendarComponent } from './pages/study-group-calendar/study-group-calendar.component';
import { StudyGroupStatsComponent } from './pages/study-group-stats/study-group-stats.component';
import { StudyGroupAuditComponent } from './pages/study-group-audit/study-group-audit.component';
import { NotificationBellComponent } from './components/notification-bell/notification-bell.component'; // ✅
import {ChatbotComponent} from "./chatbot/chatbot.component";


@NgModule({
  declarations: [
    StudyGroupListComponent,
    StudyGroupFormComponent,
    StudyGroupDetailsComponent,
    StudyGroupCalendarComponent,
    StudyGroupStatsComponent,
    StudyGroupAuditComponent,
    NotificationBellComponent,
    ChatbotComponent,
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    ReactiveFormsModule,
    RouterModule,
    StudyGroupsRoutingModule,
    FormsModule,
  ],
  exports: [
    NotificationBellComponent
  ]
})
export class StudyGroupsModule { }
