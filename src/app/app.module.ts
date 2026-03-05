import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { SharedModule } from './features/shared/shared.module';
import { HttpClientModule } from '@angular/common/http';
import { UserModule } from './features/user-module';
import { AdminModule } from './features/admin/admin-module';
import { TutorModule } from './features/tutor/tutor-module';
import { StudentModule } from './features/student/student.module';
import { AppointmentModule } from './features/Appointment/appointment.module'; // <--- Importe-le
import { VisitorModule } from './features/visitor/visitor.module'; // Importe-le ici
import { ChatWidgetComponent } from './features/shared/chat-widget/chat-widget';
import { DiscussionMgmtComponent } from './features/discussion-mgmt/discussion-mgmt';


@NgModule({
  declarations: [
    AppComponent,
    ChatWidgetComponent ,
    DiscussionMgmtComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    RouterModule ,
    HttpClientModule,
    TutorModule,
    StudentModule,
    AppointmentModule,
    VisitorModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
