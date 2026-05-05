import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { StudyGroupListComponent } from './pages/study-group-list/study-group-list.component';
import { StudyGroupFormComponent } from './pages/study-group-form/study-group-form.component';
import { StudyGroupDetailsComponent } from './pages/study-group-details/study-group-details.component';
import { StudyGroupCalendarComponent } from './pages/study-group-calendar/study-group-calendar.component';
import { StudyGroupStatsComponent } from './pages/study-group-stats/study-group-stats.component';
import { StudyGroupAuditComponent } from './pages/study-group-audit/study-group-audit.component';
import { NotificationBellComponent } from './components/notification-bell/notification-bell.component';
import { ChatbotComponent } from './chatbot/chatbot.component';

const routes: Routes = [
  { path: '',          component: StudyGroupListComponent },
  { path: 'new',       component: StudyGroupFormComponent },
  { path: 'calendar',  component: StudyGroupCalendarComponent },
  { path: 'stats',     component: StudyGroupStatsComponent },
  { path: 'edit/:id',  component: StudyGroupFormComponent },
  { path: ':id/audit', component: StudyGroupAuditComponent },
  { path: ':id',       component: StudyGroupDetailsComponent },
];

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
    FormsModule,
    RouterModule.forChild(routes),
  ],
  exports: [
    NotificationBellComponent,
    ChatbotComponent,
  ]
})
export class StudyGroupsModule { }
