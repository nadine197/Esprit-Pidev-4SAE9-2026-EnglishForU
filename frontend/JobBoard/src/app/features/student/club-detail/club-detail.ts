import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubService } from '../../../services/club.service';
import { EventService } from '../../../services/event.service';
import { FeedbackService } from '../../../services/feedback.service';

@Component({
  selector: 'app-club-detail',
  templateUrl: './club-detail.html'
})
export class ClubDetailComponent implements OnInit {
  club: any = null;
  members: any[] = [];
  pendingMembers: any[] = [];
  events: any[] = [];
  loading = true;
  activeSection = 'about';

  clubReviews: any[] = [];
  myClubReview: any = null;
  canReviewClub = false;
  clubRating = 0;
  clubRatingHover = 0;
  clubComment = '';
  submittingClubReview = false;
  clubReviewError = '';
  clubReviewSuccess = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clubService: ClubService,
    private eventService: EventService,
    private feedbackService: FeedbackService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadClub(id);
  }

  loadClub(id: number) {
    this.loading = true;
    this.clubService.getClubById(id).subscribe({
      next: data => {
        this.club = data;
        this.loadMembers(id);
        this.loadEvents(id);
        if (data.isPresident) {
          this.loadPendingMembers(id);
        }
        this.loadClubFeedback(id);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  loadClubFeedback(id: number) {
    this.feedbackService.getClubFeedback(id).subscribe({
      next: (data) => {
        this.clubReviews = data.reviews || [];
        this.myClubReview = data.myReview;
        this.canReviewClub = data.canReview;
      },
      error: () => {}
    });
  }

  loadMembers(id: number) {
    this.clubService.getMembers(id).subscribe(data => this.members = data);
  }

  loadPendingMembers(id: number) {
    this.clubService.getPendingMembers(id).subscribe(data => this.pendingMembers = data);
  }

  loadEvents(id: number) {
    this.eventService.getEventsByClub(id).subscribe(data => this.events = data);
  }

  joinClub() {
    this.clubService.joinClub(this.club.id).subscribe({
      next: () => this.loadClub(this.club.id),
      error: (err: any) => alert(err.error?.message || 'Failed to join')
    });
  }

  acceptMember(membershipId: number) {
    this.clubService.acceptMember(this.club.id, membershipId).subscribe(() => {
      this.loadClub(this.club.id);
    });
  }

  rejectMember(membershipId: number) {
    this.clubService.rejectMember(this.club.id, membershipId).subscribe(() => {
      this.loadClub(this.club.id);
    });
  }

  deleteClub() {
    if (confirm('Are you sure you want to delete this club?')) {
      this.clubService.deleteClub(this.club.id).subscribe(() => {
        this.router.navigate(['/student/clubs']);
      });
    }
  }

  goToEvent(eventId: number) {
    this.router.navigate(['/student/events', eventId]);
  }

  setClubRating(star: number) { this.clubRating = star; }
  hoverClubRating(star: number) { this.clubRatingHover = star; }
  clearClubHover() { this.clubRatingHover = 0; }

  clubStarClass(star: number): string {
    const active = this.clubRatingHover > 0 ? this.clubRatingHover : this.clubRating;
    return star <= active ? 'text-yellow-400' : 'text-gray-300';
  }

  displayClubStarClass(star: number, rating: number): string {
    return star <= rating ? 'text-yellow-400' : 'text-gray-200';
  }

  starsArray(): number[] { return [1, 2, 3, 4, 5]; }

  submitClubReview(): void {
    if (this.clubRating === 0) { this.clubReviewError = 'Please select a rating.'; return; }
    this.submittingClubReview = true;
    this.clubReviewError = '';
    this.clubReviewSuccess = '';
    this.feedbackService.submitClubFeedback(this.club.id, this.clubRating, this.clubComment).subscribe({
      next: () => {
        this.clubReviewSuccess = 'Thank you for rating this club!';
        this.clubRating = 0;
        this.clubComment = '';
        this.submittingClubReview = false;
        this.loadClubFeedback(this.club.id);
      },
      error: (err) => {
        this.clubReviewError = err.error?.error || 'Failed to submit review.';
        this.submittingClubReview = false;
      }
    });
  }

  deleteClubReview(): void {
    if (!this.myClubReview) return;
    this.feedbackService.deleteFeedback(this.myClubReview.id).subscribe({
      next: () => this.loadClubFeedback(this.club.id),
      error: () => {}
    });
  }

  getInitials(name: string): string {
    if (!name) return '?';
    return name.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2);
  }

  getAvatarColor(name: string): string {
    const colors = ['bg-emerald-500', 'bg-blue-500', 'bg-purple-500', 'bg-rose-500', 'bg-amber-500'];
    return colors[name ? name.charCodeAt(0) % colors.length : 0];
  }
}

