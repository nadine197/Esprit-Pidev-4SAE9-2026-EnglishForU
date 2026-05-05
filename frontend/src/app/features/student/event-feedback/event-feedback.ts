import { Component, Input, OnInit } from '@angular/core';
import { FeedbackService } from '../../../services/feedback.service';

@Component({
  selector: 'app-event-feedback',
  templateUrl: './event-feedback.html'
})
export class EventFeedbackComponent implements OnInit {
  @Input() eventId!: number;

  reviews: any[] = [];
  myReview: any = null;
  canReview = false;
  loading = true;
  submitting = false;
  deleting = false;
  error = '';
  successMsg = '';

  selectedRating = 0;
  hoveredRating = 0;
  comment = '';

  constructor(private feedbackService: FeedbackService) {}

  ngOnInit(): void {
    this.loadFeedback();
  }

  loadFeedback(): void {
    this.loading = true;
    this.feedbackService.getEventFeedback(this.eventId).subscribe({
      next: (data) => {
        this.reviews = data.reviews || [];
        this.myReview = data.myReview;
        this.canReview = data.canReview;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  setRating(star: number): void {
    this.selectedRating = star;
  }

  hoverRating(star: number): void {
    this.hoveredRating = star;
  }

  clearHover(): void {
    this.hoveredRating = 0;
  }

  starClass(star: number): string {
    const active = this.hoveredRating > 0 ? this.hoveredRating : this.selectedRating;
    return star <= active ? 'text-yellow-400' : 'text-gray-300';
  }

  displayStarClass(star: number, rating: number): string {
    return star <= rating ? 'text-yellow-400' : 'text-gray-200';
  }

  submitFeedback(): void {
    if (this.selectedRating === 0) {
      this.error = 'Please select a star rating.';
      return;
    }
    this.submitting = true;
    this.error = '';
    this.successMsg = '';
    this.feedbackService.submitEventFeedback(this.eventId, this.selectedRating, this.comment).subscribe({
      next: () => {
        this.successMsg = 'Thank you for your feedback!';
        this.selectedRating = 0;
        this.comment = '';
        this.submitting = false;
        this.loadFeedback();
      },
      error: (err) => {
        this.error = err.error?.error || 'Failed to submit feedback.';
        this.submitting = false;
      }
    });
  }

  deleteMyReview(): void {
    if (!this.myReview) return;
    this.deleting = true;
    this.feedbackService.deleteFeedback(this.myReview.id).subscribe({
      next: () => {
        this.deleting = false;
        this.loadFeedback();
      },
      error: () => {
        this.deleting = false;
      }
    });
  }

  starsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  getInitials(name: string): string {
    if (!name) return '?';
    return name.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2);
  }

  getAvatarColor(name: string): string {
    const colors = ['bg-emerald-500', 'bg-blue-500', 'bg-purple-500', 'bg-rose-500', 'bg-amber-500', 'bg-cyan-500'];
    const idx = name ? name.charCodeAt(0) % colors.length : 0;
    return colors[idx];
  }
}
