import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './user/login/login';
import { RegisterComponent } from './user/register/register';
import { ForgotPassword } from './user/forgot-password/forgot-password';
import { MainComponent } from './user/main/main';
<<<<<<< HEAD

=======
import { CoursesComponent } from './courses/courses';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPassword },
<<<<<<< HEAD
  { path: 'main', component: MainComponent }
=======
  { path: 'main', component: MainComponent },
    { path: 'courses', component: CoursesComponent }
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
