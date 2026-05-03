import { Component, OnInit } from '@angular/core';
import { AppointmentService } from '../../../services/appointment.service';

@Component({
  selector: 'app-appointment-mgmt',
  templateUrl: './appointment-mgmt.html'
})
export class AppointmentMgmtComponent implements OnInit {
  appointments: any[] = [];
  loading = false;
  sortDirection: 'asc' | 'desc' = 'desc';
  filterStatus: string = '';
filterLocation: string = '';
filterSuspicious: boolean = false;
  // Edit & Slot state
  editingApptId: string | null = null;
  tempDate: string = '';
  slotSuccessMessage: string = '';
  newSlot = {
    startTime: '',
    endTime: ''
  };
  
  // Pagination, Search & Sort state
  searchTerm: string = '';
  currentPage: number = 0;
  pageSize: number = 5;
  totalPages: number = 0;
  sortField: string = 'appointmentDate';
  sortDir: string = 'desc';

  constructor(private apptService: AppointmentService) {}

  ngOnInit() {
    this.loadAll();
  }

  // Updated to support Pagination & Search
  loadAll() {
    this.loading = true;
    this.apptService.getAppointmentsPaged(
      this.currentPage, 
      this.pageSize, 
      this.searchTerm, 
      this.sortField, 
      this.sortDir,
      this.filterStatus,      // Nouveau
    this.filterLocation,    // Nouveau
    this.filterSuspicious   // Nouveau
    ).subscribe({
      next: (res: any) => {
        // We assume the backend returns a Spring Data Page object
        this.appointments = res.content; 
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error("Error loading appointments:", err);
        this.loading = false;
      }
    });
  }

  nextPage() {
    if (this.currentPage + 1 < this.totalPages) {
      this.currentPage++;
      this.loadAll();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadAll();
    }
  }

  // --- CRUD Operations ---

  onAccept(id: string) {
    this.apptService.acceptAppointment(id).subscribe({
      next: () => {
        this.loadAll();
      }
    });
  }

  onCancel(id: string) {
    if (confirm("Cancel this appointment?")) {
      this.apptService.cancelAppointment(id).subscribe({
        next: () => this.loadAll()
      });
    }
  }

  onComplete(id: string) {
  const grade = prompt("Enter student English Level (e.g. A1, B2, C1):");
  if (grade) {
    // On ajoute '0' comme 4ème argument pour le cheatCount (puisque c'est une validation manuelle)
    this.apptService.completeAppointment(id, grade, 'Manual', 0).subscribe({
      next: () => {
        this.loadAll();
      },
      error: (err) => {
        console.error("Error manual completion:", err);
        alert("Failed to complete appointment.");
      }
    });
  }
}

  addSlot() {
    const start = new Date(this.newSlot.startTime);
    const end = new Date(this.newSlot.endTime);
    const now = new Date();

    if (start < now) {
      alert("You cannot create a slot in the past!");
      return;
    }

    if (end <= start) {
      alert("Error: The End Time must be later than the Start Time.");
      return;
    }

    const slotToSend = {
      startTime: this.newSlot.startTime,
      endTime: this.newSlot.endTime,
      isBooked: false
    };

    this.apptService.addSlot(slotToSend).subscribe({
      next: () => {
        this.slotSuccessMessage = 'Slot added successfully!';
        this.newSlot = { startTime: '', endTime: '' };
        setTimeout(() => this.slotSuccessMessage = '', 3000);
      },
      error: (err) => alert("Failed to add slot.")
    });
  }

  // --- Reschedule Methods ---

  startReschedule(appt: any) {
    this.editingApptId = appt.id;
    this.tempDate = appt.appointmentDate;
  }

  saveNewDate(id: string) {
    this.apptService.rescheduleAppointment(id, this.tempDate).subscribe({
      next: () => {
        this.editingApptId = null;
        this.loadAll();
      }
    });
  }

  // Dans appointment-mgmt.ts

sort(field: string) {
  if (this.sortField === field) {
    // On change : si c'était 'asc' ça devient 'desc' et vice versa
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
  } else {
    // Si on change de colonne, on commence par 'asc'
    this.sortField = field;
    this.sortDirection = 'asc';
  }
  
  console.log(`Tri appliqué : ${this.sortField} en mode ${this.sortDirection}`);
  this.currentPage = 0; // Toujours revenir à la page 1 quand on trie
  this.loadAll();
}

// Pour éviter de saturer le serveur (les 10 requêtes dans ton log)
onSearch() {
  this.currentPage = 0;
  this.loadAll();
}

resetFilters() {
  this.filterStatus = '';
  this.filterLocation = '';
  this.filterSuspicious = false;
  this.searchTerm = '';
  this.loadAll();
}
}