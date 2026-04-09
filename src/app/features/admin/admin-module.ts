import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { RouterModule } from '@angular/router';
import { SharedModule } from '..//shared/shared.module';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UserListComponent } from './user-list/user-list'; 
import { ReactiveFormsModule } from '@angular/forms';
<<<<<<< HEAD
=======
import { QuizManagementComponent } from './quiz-management/quiz-management.component';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

@NgModule({
  declarations: [
    AdminLayoutComponent,
    DashboardComponent,
<<<<<<< HEAD
    UserListComponent 
=======
    UserListComponent,
    QuizManagementComponent
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  ],
  imports: [
    CommonModule,    
    SharedModule,    
    RouterModule,
    ReactiveFormsModule,
      
  ]
})
<<<<<<< HEAD
export class AdminModule { }
=======
export class AdminModule { }
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
