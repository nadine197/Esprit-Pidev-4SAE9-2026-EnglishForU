import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { ClubService } from '../../../services/club.service';
import { EventService } from '../../../services/event.service';

@Component({
  selector: 'app-student-home',
  templateUrl: './student-home.html'
})
export class StudentHomeComponent implements OnInit {
  currentUser: any;
  recentClubs: any[] = [];
  upcomingEvents: any[] = [];
  features = [
    { title: 'My Clubs', desc: 'Join communities and meet people.', icon: '🏫', link: '/student/clubs' },
    { title: 'Events', desc: 'Discover upcoming activities.', icon: '🎉', link: '/student/events' },
    { title: 'My Courses', desc: 'Continue your learning journey.', icon: '📚', link: '/courses' },
    { title: 'Certificates', desc: 'Download your diplomas.', icon: '🏆', link: null }
  ];

  constructor(
    private authService: AuthService,
    private clubService: ClubService,
    private eventService: EventService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.clubService.getAllClubs().subscribe({
      next: data => this.recentClubs = data.slice(0, 3),
      error: () => {}
    });
    this.eventService.getAllEvents().subscribe({
      next: data => this.upcomingEvents = data.slice(0, 3),
      error: () => {}
    });
  }

  get displayName(): string {
    if (!this.currentUser) return '';
    const parts = [this.currentUser.name, this.currentUser.lastName].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : this.currentUser.email || 'Student';
  }
}

