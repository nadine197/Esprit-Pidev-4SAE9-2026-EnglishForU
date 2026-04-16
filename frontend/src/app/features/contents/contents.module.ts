import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {ReactiveFormsModule} from "@angular/forms";
import { ContentsRoutingModule } from './contents-routing.module';

import { ContentListComponent } from './pages/content-list/content-list.component';
import { ContentFormComponent } from './pages/content-form/content-form.component';
import { ContentDetailsComponent } from './pages/content-details/content-details.component';

@NgModule({
  declarations: [
    ContentListComponent,
    ContentFormComponent,
    ContentDetailsComponent
  ],
  imports: [
    CommonModule,
    ContentsRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class ContentsModule { }
