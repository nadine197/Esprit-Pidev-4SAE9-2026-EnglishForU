import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DragDropModule } from '@angular/cdk/drag-drop';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { SharedModule } from './features/shared/shared.module';
import { CoursesComponent } from './features/courses/courses';
import { HttpClientModule } from '@angular/common/http';
import { UserModule } from './features/user-module';
import { AdminModule } from './features/admin/admin-module';
import { TutorModule } from './features/tutor/tutor-module';
import { StudentModule } from './features/student/student.module';
import { HelpdeskBoardComponent } from './features/helpdesk/board/helpdesk-board';
import { DiscussionFeedComponent } from './features/discussions/feed/discussion-feed';

@NgModule({
  declarations: [
    AppComponent,
    CoursesComponent,
    HelpdeskBoardComponent,
    DiscussionFeedComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    SharedModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    HttpClientModule,
    DragDropModule,
    TutorModule,
    StudentModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
