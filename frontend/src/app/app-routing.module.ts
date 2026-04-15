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
import { CourseDetailsComponent } from './features/courses/course-details/course-details.component';
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },
  { path: 'courses', component: CoursesComponent },

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
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'promos', component: PromoManagementComponent },
            { path: 'payments', component: PaymentManagementComponent },
            { path: 'appointments', component: AppointmentMgmtComponent },
      { path: 'discussions', component: DiscussionMgmtComponent }, 
      { path: 'chat', component: ChatWidgetComponent }, 


{ path: 'packages', component: PackageManagementComponent },
{       path: 'users/:role',component: UserListComponent 
},      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
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