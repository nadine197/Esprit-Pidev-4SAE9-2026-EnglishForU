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
import { CourseDetailsComponent } from './features/courses/course-details/course-details.component';
import { QuizComponent } from './features/quiz/quiz/quiz.component';
import { AddQuizComponent } from './features/quiz/add-quiz/add-quiz.component';
import { QuizDetailsComponent } from './features/quiz/quiz-details/quiz-details.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },
  { path: 'courses', component: CoursesComponent },
  { path: 'coursesDetails/:id', component: CourseDetailsComponent },
  { path: 'quiz/:id', component: QuizComponent },
  { path: 'AddQuiz/:courseId', component: AddQuizComponent },
  { path: 'quizDetails/:id', component: QuizDetailsComponent },

  { 
    path: 'admin', 
    component: AdminLayoutComponent, 
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UserListComponent }, // <--- Add this!
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
  path: 'tutor',
  component: TutorLayoutComponent,
  canActivate: [authGuard],
  data: { roles: ['TUTOR'] }, // Only Tutors allowed
  children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', component: TutorDashboardComponent },
    { path: 'courses', component: CoursesComponent },

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