import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CoursesRoutingModule } from './courses-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CourseListComponent } from './pages/course-list/course-list.component';
import { CourseFormComponent } from './pages/course-form/course-form.component';
import { CourseDetailsComponent } from './pages/course-details/course-details.component';
import { CoursesComponent } from './courses';

import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    CourseListComponent,
    CourseFormComponent,
    CourseDetailsComponent,
    CoursesComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    CoursesRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule
  ]
})
export class CoursesModule {}
