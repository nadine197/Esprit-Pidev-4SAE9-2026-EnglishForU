import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CoursesRoutingModule } from './courses-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { CourseListComponent } from './pages/course-list/course-list.component';
import { CourseFormComponent } from './pages/course-form/course-form.component';
import { CourseDetailsComponent } from './pages/course-details/course-details.component';

@NgModule({
  declarations: [
    CourseListComponent,
    CourseFormComponent,
    CourseDetailsComponent
  ],
  imports: [
    CommonModule,
    CoursesRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class CoursesModule {}
