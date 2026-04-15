import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { StudyGroupService } from '../../../../services/study-group.service';
import { StudyGroup } from '../../models/study-group';
import {
  StudyGroupNotificationService,
  GroupNotification
} from '../../../../services/study-group-notification.service';

@Component({
  selector: 'app-study-group-list',
  templateUrl: './study-group-list.component.html',
  styleUrls: ['./study-group-list.component.css']
})
export class StudyGroupListComponent implements OnInit, OnDestroy {

  groups:        StudyGroup[] = [];
  allGroups:     StudyGroup[] = [];
  loading        = false;
  searching      = false;
  error: string | null = null;
  showFilters    = false;

  filterForm!: FormGroup;

  readonly LEVELS   = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'BEGINNER'];
  readonly STATUSES = ['ACTIVE', 'PLANNED', 'COMPLETED', 'CANCELLED'];

  // ✅ Subscription pour les notifications
  private notifSub!: Subscription;

  constructor(
    private service:      StudyGroupService,
    private router:       Router,
    private fb:           FormBuilder,
    private notifService: StudyGroupNotificationService  // ✅ ajouté
  ) {}

  ngOnInit(): void {
    this.filterForm = this.fb.group({
      name:     [''],
      level:    [''],
      status:   [''],
      location: [''],
      courseId: [null]
    });

    this.loadGroups();

    // Recherche automatique avec debounce
    this.filterForm.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => this.applyFilters());

    // ✅ Refresh automatique quand un statut change
    this.notifSub = this.notifService.notifications$.subscribe(
      (notif: GroupNotification) => {
        if (notif.type === 'STATUS_CHANGED') {
          this.loadGroups();
        }
      }
    );
  }

  loadGroups(): void {
    this.loading = true;
    this.service.getAll().subscribe({
      next: data => {
        this.groups    = data;
        this.allGroups = data;
        this.loading   = false;
      },
      error: () => {
        this.error   = 'Failed to load study groups';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    const v = this.filterForm.value;

    const isEmpty = !v.name && !v.level && !v.status
      && !v.location && !v.courseId;

    if (isEmpty) {
      this.groups = this.allGroups;
      return;
    }

    this.searching = true;
    this.service.search({
      name:     v.name     || undefined,
      level:    v.level    || undefined,
      status:   v.status   || undefined,
      location: v.location || undefined,
      courseId: v.courseId || undefined
    }).subscribe({
      next: data => {
        this.groups    = data;
        this.searching = false;
      },
      error: () => {
        this.error     = 'Erreur lors de la recherche.';
        this.searching = false;
      }
    });
  }

  resetFilters(): void {
    this.filterForm.reset({
      name: '', level: '', status: '', location: '', courseId: null
    });
    this.groups = this.allGroups;
  }

  get hasActiveFilters(): boolean {
    const v = this.filterForm.value;
    return !!(v.name || v.level || v.status || v.location || v.courseId);
  }

  delete(id: number): void {
    if (!id) return;
    if (confirm('Delete this study group?')) {
      this.service.delete(id).subscribe(() => this.loadGroups());
    }
  }

  openCalendar(): void { this.router.navigate(['/study-groups/calendar']); }
  openStats():    void { this.router.navigate(['/study-groups/stats']);    }

  // ✅ Déconnexion propre
  ngOnDestroy(): void {
    this.notifSub?.unsubscribe();
  }
  showChatbot: boolean = false;
  toggleChatbot() {
    this.showChatbot = !this.showChatbot;
  }
}
