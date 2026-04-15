import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StudyGroupService } from '../../../../services/study-group.service';

@Component({
  selector: 'app-study-group-stats',
  templateUrl: './study-group-stats.component.html',
  styleUrls: ['./study-group-stats.component.css']
})
export class StudyGroupStatsComponent implements OnInit {

  stats: any = null;
  loading = true;
  error   = '';

  levelLabels:    string[] = [];
  levelCounts:    number[] = [];
  levelFillRates: number[] = [];
  statusLabels:   string[] = [];
  statusCounts:   number[] = [];
  monthLabels:    string[] = [];
  monthCounts:    number[] = [];

  readonly MONTH_NAMES: Record<string, string> = {
    '1':'Jan','2':'Fév','3':'Mar','4':'Avr',
    '5':'Mai','6':'Jun','7':'Jul','8':'Aoû',
    '9':'Sep','10':'Oct','11':'Nov','12':'Déc'
  };

  constructor(
    public service: StudyGroupService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.service.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.prepareChartData(data);
        this.loading = false;
      },
      error: () => {
        this.error   = 'Impossible de charger les statistiques.';
        this.loading = false;
      }
    });
  }

  prepareChartData(data: any): void {
    this.levelLabels    = Object.keys(data.countByLevel   ?? {});
    this.levelCounts    = Object.values(data.countByLevel ?? {}) as number[];
    this.levelFillRates = this.levelLabels.map(
      l => data.fillRateByLevel?.[l] ?? 0
    );

    this.statusLabels = Object.keys(data.countByStatus   ?? {});
    this.statusCounts = Object.values(data.countByStatus ?? {}) as number[];

    this.monthLabels = Object.keys(data.countByMonth ?? {})
      .map(m => this.MONTH_NAMES[m] ?? m);
    this.monthCounts = Object.values(data.countByMonth ?? {}) as number[];
  }

  getStatusColor(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: '#1D9E75', PLANNED: '#185FA5',
      COMPLETED: '#534AB7', CANCELLED: '#E24B4A'
    };
    return map[status] ?? '#888780';
  }

  getFillColor(pct: number): string {
    if (pct >= 90) return '#E24B4A';
    if (pct >= 60) return '#BA7517';
    return '#1D9E75';
  }

  getBarWidth(value: number, max: number): number {
    return max > 0 ? Math.round((value / max) * 100) : 0;
  }

  get maxLevelCount(): number {
    return Math.max(...this.levelCounts, 1);
  }

  get maxMonthCount(): number {
    return Math.max(...this.monthCounts, 1);
  }

  objectKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  goBack(): void {
    this.router.navigate(['/study-groups']);
  }
}
