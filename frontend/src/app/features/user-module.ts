import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserRoutingModule } from './/user-routing-module';
import { LoginComponent } from './user/login/login';
import { RegisterComponent } from './user/register/register';
import { ForgotPassword } from './user/forgot-password/forgot-password';
import { MainComponent } from './user/main/main';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from './shared/shared.module';

import { FormsModule } from '@angular/forms';   
import { AdminLayoutComponent } from './admin/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';

@NgModule({
  declarations: [
    LoginComponent,
    RegisterComponent,
    ForgotPassword,
    MainComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    UserRoutingModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class UserModule { }
