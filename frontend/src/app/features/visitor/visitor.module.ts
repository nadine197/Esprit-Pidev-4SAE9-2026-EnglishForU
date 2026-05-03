import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormsModule, ReactiveFormsModule } from '@angular/forms'; 
import { BookTestComponent } from './book-test/book-test';
import { SharedModule } from '../shared/shared.module';
import { TakeTestComponent } from './take-test/take-test.component';

@NgModule({
  declarations: [
    BookTestComponent,
    TakeTestComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SharedModule,
    FormsModule,
  ],
  exports: [
    BookTestComponent,
    TakeTestComponent
  ]
})
export class VisitorModule { }