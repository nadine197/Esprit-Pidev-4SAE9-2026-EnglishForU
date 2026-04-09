import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

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
    path: 'student-home',
    component: StudentHomeComponent,
    canActivate: [authGuard],
    data: { roles: ['STUDENT'] }
  },

  // 🔁 Default
  { path: '', redirectTo: 'main', pathMatch: 'full' },

  // ❌ Optionnel (fallback)
  // { path: '**', redirectTo: 'main' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
