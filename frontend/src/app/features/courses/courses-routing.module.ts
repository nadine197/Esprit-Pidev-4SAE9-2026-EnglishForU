import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CourseListComponent } from './pages/course-list/course-list.component';
import { CourseFormComponent } from './pages/course-form/course-form.component';
import { CourseDetailsComponent } from './pages/course-details/course-details.component';
import { CoursesComponent } from './courses';

const routes: Routes = [
  { path: '', component: CourseListComponent  },
  { path: 'list', component: CoursesComponent },
  { path: 'new', component: CourseFormComponent },
  { path: 'edit/:id', component: CourseFormComponent },
  { path: 'details/:id', component: CourseDetailsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CoursesRoutingModule {}
