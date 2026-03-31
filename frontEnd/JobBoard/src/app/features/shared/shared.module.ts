import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from './navbar/navbar';
import { FooterComponent } from './footer/footer';
import { SidebarComponent } from './sidebar/sidebar';
import { TutorSidebarComponent } from './tutor-sidebar/tutor-sidebar';

@NgModule({
  declarations: [
    NavbarComponent,
    FooterComponent,
    SidebarComponent,
    TutorSidebarComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    NavbarComponent,
    FooterComponent,
    SidebarComponent,
    CommonModule,
    RouterModule  ,
    TutorSidebarComponent
  ]
})
export class SharedModule { }
