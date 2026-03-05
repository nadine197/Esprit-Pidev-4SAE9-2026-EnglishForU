import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { RouterModule } from '@angular/router';
import { SharedModule } from '..//shared/shared.module';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UserListComponent } from './user-list/user-list'; 
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms'; // <--- FIXES ngModel


@NgModule({
  declarations: [
    AdminLayoutComponent,
    DashboardComponent,
    UserListComponent 
  ],
  imports: [
    CommonModule,    
    SharedModule,    
    RouterModule,
    ReactiveFormsModule,
      FormsModule,
  ]
})
export class AdminModule { }