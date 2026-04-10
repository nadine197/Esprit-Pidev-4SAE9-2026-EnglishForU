import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
<<<<<<< HEAD

import { LoginComponent } from './features/user/login/login';
import { RegisterComponent } from './features/user/register/register';
import { MainComponent } from './features/user/main/main';

import { AdminLayoutComponent } from './features/admin/admin-layout/admin-layout.component';
import { DashboardComponent } from './features/admin/dashboard/dashboard.component';
import { UserListComponent } from './features/admin/user-list/user-list';

import { TutorDashboardComponent } from './features/tutor/dashboard/tutor-dashboard';
import { TutorLayoutComponent } from './features/tutor/layout/tutor-layout';

import { StudentHomeComponent } from './features/student/student-home/student-home';

import { authGuard } from './guards/auth.guard';

const routes: Routes = [
  // 🔐 Auth
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },

  // 📚 Modules lazy loaded
  {
    path: 'courses',
    loadChildren: () =>
      import('./features/courses/courses.module').then(m => m.CoursesModule)
  },
  {
    path: 'contents',
    loadChildren: () =>
      import('./features/contents/contents.module').then(m => m.ContentsModule)
  },
  {
    path: 'study-groups',
    loadChildren: () =>
      import('./features/study-groups/study-groups.module')
        .then(m => m.StudyGroupsModule)
  },

  // 👨‍💼 ADMIN
=======
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
import { StudentQuizzesComponent } from './features/student/student-quizzes/student-quizzes';
import { CourseDetailsComponent } from './features/courses/course-details/course-details.component';
import { QuizComponent } from './features/quiz/quiz/quiz.component';
import { AddQuizComponent } from './features/quiz/add-quiz/add-quiz.component';
import { QuizDetailsComponent } from './features/quiz/quiz-details/quiz-details.component';
import { QuizManagementComponent } from './features/admin/quiz-management/quiz-management.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },
  { path: 'courses', component: CoursesComponent },
  { path: 'coursesDetails/:id', component: CourseDetailsComponent },
  { path: 'quiz/:id', component: QuizComponent },
  { path: 'AddQuiz/:courseId', component: AddQuizComponent },
  { path: 'quizDetails/:id', component: QuizDetailsComponent },

>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
<<<<<<< HEAD
      { path: 'users', component: UserListComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // 👨‍🏫 TUTOR
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

  // 🎓 STUDENT
  {
=======
      { path: 'users', component: UserListComponent }, // <--- Add this!
      { path: 'quizzes', component: QuizManagementComponent },
      { path: 'quizzes/list', component: QuizComponent },
      { path: 'quizzes/course/:id', component: CourseDetailsComponent },
      { path: 'quizzes/create/:courseId', component: AddQuizComponent },
      { path: 'quizzes/list/:id', component: QuizComponent },
      { path: 'quizzes/details/:id', component: QuizDetailsComponent },
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
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
    path: 'student-home',
    component: StudentHomeComponent,
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] }
  },
<<<<<<< HEAD

  // 🔁 Default
  { path: '', redirectTo: 'main', pathMatch: 'full' },

  // ❌ Optionnel (fallback)
  // { path: '**', redirectTo: 'main' }
];

=======
  {
    path: 'student-quizzes',
    component: StudentQuizzesComponent,
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] }
  },
  { path: '', redirectTo: 'main', pathMatch: 'full' }
];

// ... imports ...
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
<<<<<<< HEAD
export class AppRoutingModule {}
=======
export class AppRoutingModule { } // <--- MUST HAVE 'export'
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
