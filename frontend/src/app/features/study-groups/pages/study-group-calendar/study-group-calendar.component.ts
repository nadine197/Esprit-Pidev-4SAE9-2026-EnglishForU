// src/app/features/study-groups/pages/study-group-calendar/study-group-calendar.component.ts

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StudyGroupService } from '../../../../services/study-group.service';
import { StudyGroup, MarkedDates } from '../../models/study-group';  // ← StudyGroupResponseDTO supprimé

@Component({
  selector: 'app-study-group-calendar',
  templateUrl: './study-group-calendar.component.html',
  styleUrls: ['./study-group-calendar.component.css']
})
export class StudyGroupCalendarComponent implements OnInit {

  currentYear  = new Date().getFullYear();
  currentMonth = new Date().getMonth() + 1;

  readonly MONTHS = [
    'Janvier','Février','Mars','Avril','Mai','Juin',
    'Juillet','Août','Septembre','Octobre','Novembre','Décembre'
  ];
  readonly WEEK_DAYS = ['Lun','Mar','Mer','Jeu','Ven','Sam','Dim'];

  get monthLabel(): string {
    return `${this.MONTHS[this.currentMonth - 1]} ${this.currentYear}`;
  }

  daysInMonth:   number[] = [];
  leadingBlanks: null[]   = [];

  markedDates: MarkedDates = {};
  allGroupsOfMonth: StudyGroup[] = [];   // ← StudyGroupResponseDTO → StudyGroup

  selectedDate:   Date | null = null;
  selectedGroups: StudyGroup[] = [];     // ← StudyGroupResponseDTO → StudyGroup
  loading         = false;
  loadingCalendar = false;

  constructor(
    public calService: StudyGroupService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMonth();
  }

  loadMonth(): void {
    this.buildGrid();
    this.loadingCalendar = true;

    this.calService.getMarkedDates(this.currentYear, this.currentMonth)
      .subscribe({
        next:  m  => { this.markedDates = m; this.loadingCalendar = false; },
        error: () => { this.loadingCalendar = false; }
      });

    this.calService.getByMonth(this.currentYear, this.currentMonth)
      .subscribe(g => this.allGroupsOfMonth = g);
  }

  buildGrid(): void {
    const total = new Date(this.currentYear, this.currentMonth, 0).getDate();
    this.daysInMonth = Array.from({ length: total }, (_, i) => i + 1);
    let first = new Date(this.currentYear, this.currentMonth - 1, 1).getDay();
    first = first === 0 ? 6 : first - 1;
    this.leadingBlanks = Array(first).fill(null);
  }

  onDayClick(day: number): void {
    this.selectedDate   = new Date(this.currentYear, this.currentMonth - 1, day);
    this.loading        = true;
    this.selectedGroups = [];

    this.calService.getByDate(this.selectedDate).subscribe({
      next:  g  => { this.selectedGroups = g; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  prevMonth(): void {
    if (this.currentMonth === 1) { this.currentMonth = 12; this.currentYear--; }
    else this.currentMonth--;
    this.resetSelection();
    this.loadMonth();
  }

  nextMonth(): void {
    if (this.currentMonth === 12) { this.currentMonth = 1; this.currentYear++; }
    else this.currentMonth++;
    this.resetSelection();
    this.loadMonth();
  }

  goBack(): void {
    this.router.navigate(['/study-groups']);
  }

  private resetSelection(): void {
    this.selectedDate   = null;
    this.selectedGroups = [];
  }

  isToday(day: number): boolean {
    const t = new Date();
    return t.getFullYear() === this.currentYear
      && t.getMonth() + 1 === this.currentMonth
      && t.getDate() === day;
  }

  isSelected(day: number): boolean {
    return !!this.selectedDate
      && this.selectedDate.getDate() === day
      && this.selectedDate.getMonth() + 1 === this.currentMonth
      && this.selectedDate.getFullYear() === this.currentYear;
  }

  isInRange(day: number): boolean {
    const d = new Date(this.currentYear, this.currentMonth - 1, day);
    return this.allGroupsOfMonth.some(g =>
      d >= new Date(g.startdate) && d <= new Date(g.enddate)
    );
  }

  hasStartDate(day: number): boolean {
    return !!this.markedDates[this.getKey(day)]?.some(m => m.startsWith('start:'));
  }

  hasEndDate(day: number): boolean {
    return !!this.markedDates[this.getKey(day)]?.some(m => m.startsWith('end:'));
  }

  isStartDate(g: StudyGroup): boolean {         // ← type corrigé
    return !!this.selectedDate &&
      g.startdate === this.calService.formatDate(this.selectedDate);
  }

  isEndDate(g: StudyGroup): boolean {           // ← type corrigé
    return !!this.selectedDate &&
      g.enddate === this.calService.formatDate(this.selectedDate);
  }

  getSelectedDateLabel(): string {
    return this.selectedDate?.toLocaleDateString('fr-FR', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    }) ?? '';
  }

  trackById(_: number, g: StudyGroup) { return g.groupId; }  // ← type corrigé

  private getKey(day: number): string {
    return this.calService.formatDate(
      new Date(this.currentYear, this.currentMonth - 1, day)
    );
  }
}
