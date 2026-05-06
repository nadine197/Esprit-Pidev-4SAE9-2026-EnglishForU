import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './guards/auth.interceptor';

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
import { ChatWidgetComponent } from './features/shared/chat-widget/chat-widget';
import { AiChatWidgetComponent } from './features/shared/ai-chat-widget/ai-chat-widget';
import { FormatAiMessagePipe } from './pipes/format-ai-message.pipe';
import { DiscussionMgmtComponent } from './features/discussion-mgmt/discussion-mgmt';
import { AppointmentModule } from './features/Appointment/appointment.module';
import { VisitorModule } from './features/visitor/visitor.module';
import { QuizManagementComponent } from './features/admin/quiz-management/quiz-management.component';
import { QuizComponent } from './features/quiz/quiz/quiz.component';
import { AddQuizComponent } from './features/quiz/add-quiz/add-quiz.component';
import { QuizDetailsComponent } from './features/quiz/quiz-details/quiz-details.component';
import { StudentQuizzesComponent } from './features/student/student-quizzes/student-quizzes';
import { StudentEvaluationsComponent } from './features/admin/student-evaluations/student-evaluations.component';
import { StudentEvaluationsPageComponent } from './features/student/student-evaluations/student-evaluations.component';

@NgModule({
  declarations: [
    AppComponent,

    PromoManagementComponent,
    PackageManagementComponent,
    CheckoutComponent,
    PaymentResultComponent,
    PaymentManagementComponent,
    ChatWidgetComponent,
    AiChatWidgetComponent,
    FormatAiMessagePipe,
    DiscussionMgmtComponent,
    QuizManagementComponent,
    QuizComponent,
    AddQuizComponent,
    QuizDetailsComponent,
    StudentQuizzesComponent,
    StudentEvaluationsComponent,
    StudentEvaluationsPageComponent
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

    StudentModule,
    ReactiveFormsModule,
    FormsModule,
    AppointmentModule,
    VisitorModule,

  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
