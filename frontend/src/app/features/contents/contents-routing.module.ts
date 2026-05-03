import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ContentListComponent } from './pages/content-list/content-list.component';
import { ContentFormComponent } from './pages/content-form/content-form.component';
import { ContentDetailsComponent } from './pages/content-details/content-details.component';

const routes: Routes = [
  { path: '', component: ContentListComponent },
  { path: 'new', component: ContentFormComponent },
  { path: 'edit/:id', component: ContentFormComponent },
  { path: ':id', component: ContentDetailsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContentsRoutingModule {}
