import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.html'
})
export class EventListComponent implements OnInit {
  events: any[] = [];
  filteredEvents: any[] = [];
  loading = true;
  searchTerm = '';

  constructor(private eventService: EventService, private router: Router) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents() {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: data => { this.events = data; this.applySearch(); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  applySearch() {
    if (!this.searchTerm.trim()) {
      this.filteredEvents = this.events;
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredEvents = this.events.filter((e: any) =>
        e.title?.toLowerCase().includes(term) || e.clubName?.toLowerCase().includes(term) || e.location?.toLowerCase().includes(term)
      );
    }
  }

  goToEvent(id: number) {
    this.router.navigate(['/student/events', id]);
  }
}

