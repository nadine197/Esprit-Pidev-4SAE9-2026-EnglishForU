import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './features/user/login/login';
import { RegisterComponent } from './features/user/register/register';
import { MainComponent } from './features/user/main/main';
import { CoursesComponent } from './features/courses/courses';
import { AdminLayoutComponent } from './features/admin/admin-layout/admin-layout.component';
import { DashboardComponent } from './features/admin/dashboard/dashboard.component';
import { authGuard } from './/guards/auth.guard';
import { UserListComponent } from './features/admin/user-list/user-list';
import { TutorDashboardComponent } from './features/tutor/dashboard/tutor-dashboard';
import { TutorLayoutComponent } from './features/tutor/layout/tutor-layout';
import { StudentHomeComponent } from './features/student/student-home/student-home';
import { StudentLayoutComponent } from './features/student/student-layout/student-layout';
import { ClubListComponent } from './features/student/club-list/club-list';
import { ClubDetailComponent } from './features/student/club-detail/club-detail';
import { ClubFormComponent } from './features/student/club-form/club-form';
import { EventListComponent } from './features/student/event-list/event-list';
import { EventDetailComponent } from './features/student/event-detail/event-detail';
import { EventFormComponent } from './features/student/event-form/event-form';
import { MyTicketsComponent } from './features/student/my-tickets/my-tickets';
import { EventFeedbackComponent } from './features/student/event-feedback/event-feedback';
import { ClubStatsComponent } from './features/student/club-stats/club-stats';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },
  { path: 'courses', component: CoursesComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UserListComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
    path: 'tutor',
    component: TutorLayoutComponent,
    canActivate: [authGuard],
    data: { roles: ['TUTOR'] },
    children: [
      { path: 'dashboard', component: TutorDashboardComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
    path: 'student',
    component: StudentLayoutComponent,
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] },
    children: [
      { path: 'home', component: StudentHomeComponent },
      { path: 'clubs', component: ClubListComponent },
      { path: 'clubs/create', component: ClubFormComponent },
      { path: 'clubs/:id/edit', component: ClubFormComponent },
      { path: 'clubs/:id/create-event', component: EventFormComponent },
      { path: 'clubs/:id', component: ClubDetailComponent },
      { path: 'events', component: EventListComponent },
      { path: 'events/:id/edit', component: EventFormComponent },
      { path: 'events/:id', component: EventDetailComponent },
      { path: 'my-tickets', component: MyTicketsComponent },
      { path: 'clubs/:id/stats', component: ClubStatsComponent },
      { path: 'events/:id/feedback', component: EventFeedbackComponent },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },
  { path: 'student-home', redirectTo: 'student/home' },
  { path: '', redirectTo: 'main', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

