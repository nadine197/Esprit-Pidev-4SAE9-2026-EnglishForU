import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { AppointmentMgmtComponent } from './appointment-mgmt/appointment-mgmt';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    AppointmentMgmtComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SharedModule
  ],
  exports: [
    AppointmentMgmtComponent
  ]
})
export class AppointmentModule { }