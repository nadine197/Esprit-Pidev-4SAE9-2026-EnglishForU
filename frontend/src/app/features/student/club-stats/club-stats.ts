import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FeedbackService } from '../../../services/feedback.service';
import { ClubService } from '../../../services/club.service';

declare const Chart: any;

@Component({
  selector: 'app-club-stats',
  templateUrl: './club-stats.html'
})
export class ClubStatsComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('eventSentimentChart') eventSentimentRef!: ElementRef;
  @ViewChild('distributionChart') distributionRef!: ElementRef;
  @ViewChild('clubSentimentChart') clubSentimentRef!: ElementRef;

  clubId!: number;
  club: any = null;
  stats: any = null;
  clubFeedbackData: any = null;
  loading = true;
  error = '';

  private charts: any[] = [];
  private chartsReady = false;
  private dataReady = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private feedbackService: FeedbackService,
    private clubService: ClubService
  ) { }

  ngOnInit(): void {
    this.clubId = +this.route.snapshot.paramMap.get('id')!;
    this.loadData();
  }

  ngAfterViewInit(): void {
    this.chartsReady = true;
    if (this.dataReady) {
      this.renderCharts();
    }
  }

  ngOnDestroy(): void {
    this.charts.forEach(c => { try { c.destroy(); } catch (_) { } });
  }

  loadData(): void {
    this.loading = true;
    this.clubService.getClubById(this.clubId).subscribe({
      next: (club) => {
        this.club = club;
        if (!club.isPresident) {
          this.router.navigate(['/student/clubs', this.clubId]);
        }
      },
      error: () => { }
    });

    this.feedbackService.getClubStats(this.clubId).subscribe({
      next: (data) => {
        this.stats = data;
        this.feedbackService.getClubFeedback(this.clubId).subscribe({
          next: (cf) => {
            this.clubFeedbackData = cf;
            this.loading = false;
            this.dataReady = true;
            if (this.chartsReady) {
              setTimeout(() => this.renderCharts(), 0);
            }
          },
          error: () => {
            this.loading = false;
            this.dataReady = true;
            if (this.chartsReady) {
              setTimeout(() => this.renderCharts(), 0);
            }
          }
        });
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load stats.';
      }
    });
  }

  renderCharts(): void {
    this.charts.forEach(c => { try { c.destroy(); } catch (_) { } });
    this.charts = [];

    if (this.stats) {
      this.renderEventSentimentChart();
      this.renderDistributionChart();
    }
    if (this.clubFeedbackData?.reviews?.length > 0) {
      this.renderClubSentimentChart();
    }
  }

  private renderEventSentimentChart(): void {
    if (!this.eventSentimentRef) return;
    const s = this.stats;
    const chart = new Chart(this.eventSentimentRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Positive (4-5★)', 'Neutral (3★)', 'Negative (1-2★)'],
        datasets: [{
          data: [s.likePercent, s.neutralPercent, s.dislikePercent],
          backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
          borderColor: ['#059669', '#d97706', '#dc2626'],
          borderWidth: 2,
          hoverOffset: 8
        }]
      },
      options: {
        cutout: '72%',
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, font: { size: 12 } } },
          tooltip: {
            callbacks: {
              label: (ctx: any) => ` ${ctx.label}: ${ctx.raw}%`
            }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  private renderDistributionChart(): void {
    if (!this.distributionRef) return;
    const dist = this.stats.distribution || {};
    const chart = new Chart(this.distributionRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['1 ★', '2 ★', '3 ★', '4 ★', '5 ★'],
        datasets: [{
          label: 'Reviews',
          data: [dist[1] || 0, dist[2] || 0, dist[3] || 0, dist[4] || 0, dist[5] || 0],
          backgroundColor: [
            'rgba(239,68,68,0.75)',
            'rgba(249,115,22,0.75)',
            'rgba(234,179,8,0.75)',
            'rgba(34,197,94,0.75)',
            'rgba(16,185,129,0.75)'
          ],
          borderColor: ['#ef4444', '#f97316', '#eab308', '#22c55e', '#10b981'],
          borderWidth: 2,
          borderRadius: 8
        }]
      },
      options: {
        plugins: { legend: { display: false } },
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 1, font: { size: 12 } },
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: { grid: { display: false }, ticks: { font: { size: 13 } } }
        }
      }
    });
    this.charts.push(chart);
  }

  private renderClubSentimentChart(): void {
    if (!this.clubSentimentRef) return;
    const reviews: any[] = this.clubFeedbackData?.reviews || [];
    const positive = reviews.filter(r => r.rating >= 4).length;
    const neutral = reviews.filter(r => r.rating === 3).length;
    const negative = reviews.filter(r => r.rating <= 2).length;
    const total = reviews.length || 1;

    this.clubFeedbackData.likePercent = Math.round(positive * 100 / total);
    this.clubFeedbackData.neutralPercent = Math.round(neutral * 100 / total);
    this.clubFeedbackData.dislikePercent = Math.round(negative * 100 / total);

    const chart = new Chart(this.clubSentimentRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Positive', 'Neutral', 'Negative'],
        datasets: [{
          data: [
            this.clubFeedbackData.likePercent,
            this.clubFeedbackData.neutralPercent,
            this.clubFeedbackData.dislikePercent
          ],
          backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
          borderColor: ['#059669', '#d97706', '#dc2626'],
          borderWidth: 2,
          hoverOffset: 8
        }]
      },
      options: {
        cutout: '72%',
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, font: { size: 12 } } },
          tooltip: {
            callbacks: {
              label: (ctx: any) => ` ${ctx.label}: ${ctx.raw}%`
            }
          }
        }
      }
    });
    this.charts.push(chart);
  }

  starsArray(): number[] { return [1, 2, 3, 4, 5]; }

  starClass(star: number, rating: number): string {
    return star <= rating ? 'text-yellow-400' : 'text-gray-200';
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
    return colors[name ? name.charCodeAt(0) % colors.length : 0];
  }

  goBack(): void {
    this.router.navigate(['/student/clubs', this.clubId]);
  }
}
