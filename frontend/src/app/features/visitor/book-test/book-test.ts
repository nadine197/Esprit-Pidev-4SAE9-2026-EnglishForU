import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AppointmentService } from '../../../services/appointment.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-book-test',
  templateUrl: './book-test.html'
})
export class BookTestComponent implements OnInit {
  bookingForm: FormGroup;
  availableSlots: any[] = [];
  // --- Nouvelles variables pour l'accès au test ---
  accessEmail: string = '';
  accessCode: string = '';
  viewDate: Date = new Date();
  daysInMonth: Date[] = [];
  selectedDate: Date | null = null;
  slotsForSelectedDate: any[] = [];
  
  loading = false;
  success = false;

  constructor(
    private fb: FormBuilder, 
    private apptService: AppointmentService,
    private router: Router
  ) {
   // Dans book-test.ts
this.bookingForm = this.fb.group({
      visitorName: ['', Validators.required],
      visitorEmail: ['', [Validators.required, Validators.email]],
      visitorPhone: ['', [Validators.required, Validators.pattern('^[0-9]{8,15}$')]],
      appointmentDate: ['', Validators.required],
      // --- Nouveau champ pour le mode de passage ---
      locationType: ['ON_SITE', Validators.required] 
    });
  }

  ngOnInit() {
    this.generateCalendar();
    this.loadSlots();
  }

  loadSlots() {
    this.apptService.getAvailableSlots().subscribe(data => {
      this.availableSlots = data;
    });
  }

  generateCalendar() {
    const year = this.viewDate.getFullYear();
    const month = this.viewDate.getMonth();
    const lastDay = new Date(year, month + 1, 0);
    this.daysInMonth = [];
    for (let i = 1; i <= lastDay.getDate(); i++) {
      this.daysInMonth.push(new Date(year, month, i));
    }
  }

  hasSlots(date: Date): boolean {
    return this.availableSlots.some(slot => 
      new Date(slot.startTime).toDateString() === date.toDateString()
    );
  }

  selectDate(date: Date) {
    if (!this.hasSlots(date)) return;
    this.selectedDate = date;
    this.slotsForSelectedDate = this.availableSlots.filter(slot => 
      new Date(slot.startTime).toDateString() === date.toDateString()
    );
  }

  changeMonth(dir: number) {
    this.viewDate = new Date(this.viewDate.setMonth(this.viewDate.getMonth() + dir));
    this.generateCalendar();
  }

  onSubmit() {
    if (this.bookingForm.valid) {
      this.loading = true;
      this.apptService.bookTest(this.bookingForm.value).subscribe({
        next: () => {
          this.success = true;
          this.loading = false;
          setTimeout(() => this.router.navigate(['/']), 3000);
        },
        error: () => this.loading = false
      });
    }
  }

   // Dans book-test.ts
onVerifyCode() {
  this.apptService.verifyAccess(this.accessEmail, this.accessCode).subscribe({
    next: (appt) => {
      // appt contient l'objet Appointment renvoyé par le Java (avec l'ID UUID)
      console.log("Accès autorisé pour le RDV :", appt.id);
      
      // On stocke l'objet complet pour le récupérer dans la page de test
      sessionStorage.setItem('active_session', JSON.stringify(appt));
      
      // Navigation vers le test oral
      this.router.navigate(['/take-test']); 
    },
    error: (err) => {
      console.error(err);
      alert('Email or Code invalid. Please check your email.');
    }
  });
}
}