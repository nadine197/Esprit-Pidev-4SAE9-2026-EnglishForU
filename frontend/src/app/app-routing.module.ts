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
import { StudentLayoutComponent } from './features/student/student-layout/student-layout';
import { ClubListComponent } from './features/student/club-list/club-list';
import { ClubDetailComponent } from './features/student/club-detail/club-detail';
import { ClubFormComponent } from './features/student/club-form/club-form';
import { ClubStatsComponent } from './features/student/club-stats/club-stats';
import { EventListComponent } from './features/student/event-list/event-list';
import { EventDetailComponent } from './features/student/event-detail/event-detail';
import { EventFormComponent } from './features/student/event-form/event-form';
import { EventFeedbackComponent } from './features/student/event-feedback/event-feedback';
import { MyTicketsComponent } from './features/student/my-tickets/my-tickets';
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
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'promos', component: PromoManagementComponent },
      { path: 'payments', component: PaymentManagementComponent },
      { path: 'appointments', component: AppointmentMgmtComponent },
      { path: 'discussions', component: DiscussionMgmtComponent },
      { path: 'chat', component: ChatWidgetComponent },
      { path: 'packages', component: PackageManagementComponent },
      { path: 'users/:role', component: UserListComponent },
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
      { path: 'clubs/:id/stats', component: ClubStatsComponent },
      { path: 'events', component: EventListComponent },
      { path: 'events/:id/edit', component: EventFormComponent },
      { path: 'events/:id', component: EventDetailComponent },
      { path: 'events/:id/feedback', component: EventFeedbackComponent },
      { path: 'my-tickets', component: MyTicketsComponent },
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
