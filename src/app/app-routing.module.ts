import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './features/user/login/login';
import { RegisterComponent } from './features/user/register/register';
import { MainComponent } from './features/user/main/main';
import { AdminLayoutComponent } from './features/admin/admin-layout/admin-layout.component';
import { DashboardComponent } from './features/admin/dashboard/dashboard.component';
import { authGuard } from './/guards/auth.guard';
import { UserListComponent } from './features/admin/user-list/user-list';
import { TutorDashboardComponent } from './features/tutor/dashboard/tutor-dashboard';
import { TutorLayoutComponent } from './features/tutor/layout/tutor-layout';
import { StudentHomeComponent } from './features/student/student-home/student-home';
import { BookTestComponent } from './features/visitor/book-test/book-test';
import { AppointmentMgmtComponent } from './features/Appointment/appointment-mgmt/appointment-mgmt';
import { TakeTestComponent } from './features/visitor/take-test/take-test.component';
import { DiscussionMgmtComponent } from './features/discussion-mgmt/discussion-mgmt';
import { ChatWidgetComponent } from './features/shared/chat-widget/chat-widget'; // Vérifiez votre chemin

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },
  { path: 'book-test', component: BookTestComponent },
  { path: 'take-test', component: TakeTestComponent },
  { 
    path: 'admin', 
    component: AdminLayoutComponent, 
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users/:role',component: UserListComponent },
      { path: 'appointments', component: AppointmentMgmtComponent },
      { path: 'discussions', component: DiscussionMgmtComponent }, 
      { path: 'chat', component: ChatWidgetComponent }, 

      
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
  path: 'tutor',
  component: TutorLayoutComponent,
  canActivate: [authGuard],
  data: { roles: ['TUTOR'] }, // Only Tutors allowed
  children: [
    { path: 'dashboard', component: TutorDashboardComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
  ]
},
{ 
    path: 'student-home', 
    component: StudentHomeComponent, 
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] } 
  },
  { path: '', redirectTo: 'main', pathMatch: 'full' }
];

// ... imports ...
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { } // <--- MUST HAVE 'export'