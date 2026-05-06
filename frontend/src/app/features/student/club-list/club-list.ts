import { Component, OnInit } from '@angular/core';
import { ClubService } from '../../../services/club.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.html'
})
export class ClubListComponent implements OnInit {
  clubs: any[] = [];
  filteredClubs: any[] = [];
  activeTab = 'all';
  loading = true;
  searchTerm = '';

  constructor(private clubService: ClubService, private router: Router) {}

  ngOnInit(): void {
    this.loadClubs();
  }

  loadClubs() {
    this.loading = true;
    if (this.activeTab === 'my') {
      this.clubService.getMyClubs().subscribe({
        next: data => { this.clubs = data; this.applySearch(); this.loading = false; },
        error: () => { this.loading = false; }
      });
    } else {
      this.clubService.getAllClubs().subscribe({
        next: data => { this.clubs = data; this.applySearch(); this.loading = false; },
        error: () => { this.loading = false; }
      });
    }
  }

  switchTab(tab: string) {
    this.activeTab = tab;
    this.loadClubs();
  }

  applySearch() {
    if (!this.searchTerm.trim()) {
      this.filteredClubs = this.clubs;
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredClubs = this.clubs.filter((c: any) =>
        c.name?.toLowerCase().includes(term) || c.description?.toLowerCase().includes(term)
      );
    }
  }

  goToClub(id: number) {
    this.router.navigate(['/student/clubs', id]);
  }
}

