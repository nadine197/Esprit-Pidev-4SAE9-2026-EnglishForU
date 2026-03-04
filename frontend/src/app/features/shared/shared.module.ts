import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { NavbarComponent } from './navbar/navbar';
import { FooterComponent } from './footer/footer';
import { SidebarComponent } from './sidebar/sidebar';
import { TutorSidebarComponent } from './tutor-sidebar/tutor-sidebar';
import { ReportIssueComponent } from './report-issue/report-issue';
import { NotificationBellComponent } from './notification-bell/notification-bell';

@NgModule({
  declarations: [
    NavbarComponent,
    FooterComponent,
    SidebarComponent,
    TutorSidebarComponent,
    ReportIssueComponent,
    NotificationBellComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule
  ],
  exports: [
    NavbarComponent,
    FooterComponent,
    SidebarComponent,
    CommonModule,
    RouterModule,
    TutorSidebarComponent,
    ReportIssueComponent,
    NotificationBellComponent
  ]
})
export class SharedModule { }
