import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { SharedModule } from './features/shared/shared.module';


import { UserModule } from './features/user-module';
import { AdminModule } from './features/admin/admin-module';
import { TutorModule } from './features/tutor/tutor-module';
import { StudentModule } from './features/student/student.module';

import { PromoManagementComponent } from './features/admin/promo-management/promo-management.component';
import { PackageManagementComponent } from './features/admin/package-management/package-management.component';
import { CheckoutComponent } from './features/checkout/checkout.component';
import { PaymentResultComponent } from './features/payment-result/payment-result.component';
import { PaymentManagementComponent } from './features/payment-management/payment-management.component';
import { CourseDetailsComponent } from './features/courses/pages/course-details/course-details.component';
import { CoursesComponent } from './features/courses/courses';

@NgModule({
  declarations: [
    AppComponent,
    CoursesComponent,
    CourseDetailsComponent,
    PromoManagementComponent,
    PackageManagementComponent,
    CheckoutComponent,
    PaymentResultComponent,
    PaymentManagementComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    HttpClientModule,
    UserModule,
    AdminModule,
    TutorModule,
    StudentModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }