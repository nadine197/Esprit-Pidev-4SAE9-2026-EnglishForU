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
import { PackageManagementComponent } from './features/admin/package-management/package-management.component';
import { PromoManagementComponent } from './features/admin/promo-management/promo-management.component';
import { CheckoutComponent } from './features/checkout/checkout.component';
import { PaymentResultComponent } from './features/payment-result/payment-result.component';
import { PaymentManagementComponent } from './features/payment-management/payment-management.component';
import { BookTestComponent } from './features/visitor/book-test/book-test';
import { TakeTestComponent } from './features/visitor/take-test/take-test.component';
import { AppointmentMgmtComponent } from './features/Appointment/appointment-mgmt/appointment-mgmt';
import { DiscussionMgmtComponent } from './features/discussion-mgmt/discussion-mgmt';
import { ChatWidgetComponent } from './features/shared/chat-widget/chat-widget';
import { CourseDetailsComponent } from './features/courses/pages/course-details/course-details.component';
import { QuizManagementComponent } from './features/admin/quiz-management/quiz-management.component';
import { QuizComponent } from './features/quiz/quiz/quiz.component';
import { AddQuizComponent } from './features/quiz/add-quiz/add-quiz.component';
import { QuizDetailsComponent } from './features/quiz/quiz-details/quiz-details.component';
import { StudentQuizzesComponent } from './features/student/student-quizzes/student-quizzes';
import { StudentEvaluationsComponent } from './features/admin/student-evaluations/student-evaluations.component';
import { StudentEvaluationsPageComponent } from './features/student/student-evaluations/student-evaluations.component';
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },


  {
    path: 'courses',
    loadChildren: () =>
      import('./features/courses/courses.module').then(m => m.CoursesModule)
  },
    { path: 'coursesDetails/:id', component: CourseDetailsComponent },
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

   { path: 'checkout/:packageId', component: CheckoutComponent },
  { path: 'payment-result', component: PaymentResultComponent },
  { path: 'book-test', component: BookTestComponent },
  { path: 'take-test', component: TakeTestComponent },
  { 
    path: 'admin', 
    component: AdminLayoutComponent, 
    canActivate: [authGuard],
    data: { roles: ['SUPER_ADMIN', 'ADMIN'] },
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'promos', component: PromoManagementComponent },
      { path: 'payments', component: PaymentManagementComponent },
      { path: 'appointments', component: AppointmentMgmtComponent },
      { path: 'discussions', component: DiscussionMgmtComponent }, 
      { path: 'quizzes', component: QuizManagementComponent },
      { path: 'student-evaluations', component: StudentEvaluationsComponent },
      { path: 'quizzes/course/:id', component: QuizComponent },
      { path: 'quizzes/list', component: QuizComponent },
      { path: 'quizzes/details/:id', component: QuizDetailsComponent },
      { path: 'quizzes/add/:courseId', component: AddQuizComponent },
      { path: 'quizzes/:quizId/add-question', component: AddQuizComponent },
      { path: 'chat', component: ChatWidgetComponent },
      { path: 'packages', component: PackageManagementComponent },
      { path: 'users/:role', component: UserListComponent },
      // Lazy-loaded modules for admin
      { 
        path: 'courses', 
        loadChildren: () => import('./features/courses/courses.module').then(m => m.CoursesModule)
      },
      { 
        path: 'contents', 
        loadChildren: () => import('./features/contents/contents.module').then(m => m.ContentsModule)
      },
      { 
        path: 'study-groups', 
        loadChildren: () => import('./features/study-groups/study-groups.module').then(m => m.StudyGroupsModule)
      },
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
    { path: 'student-evaluations', component: StudentEvaluationsComponent },
    { path: 'quizzes/:quizId/add-question', component: AddQuizComponent },
    { path: 'AddQuiz/:courseId', component: AddQuizComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
  ]
},
{ 
    path: 'student-home', 
    component: StudentHomeComponent, 
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] } 
  },
{
    path: 'student-quizzes',
    component: StudentQuizzesComponent,
    canActivate: [authGuard],
    data: { roles: ['STUDENT', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: 'student-evaluations',
    component: StudentEvaluationsPageComponent,
    canActivate: [authGuard],
    data: { roles: ['STUDENT', 'ADMIN', 'SUPER_ADMIN'] }
  },
  { path: 'quiz/:id', component: QuizComponent },
  { path: 'quizDetails/:id', component: QuizDetailsComponent },
  { path: '', redirectTo: 'main', pathMatch: 'full' }
];

// ... imports ...
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { } // <--- MUST HAVE 'export'
