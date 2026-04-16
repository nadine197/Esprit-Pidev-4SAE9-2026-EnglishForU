
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StudyGroupService } from '../../../../services/study-group.service';

@Component({
  selector: 'app-study-group-audit',
  templateUrl: './study-group-audit.component.html',
  styleUrls: ['./study-group-audit.component.css']
})
export class StudyGroupAuditComponent implements OnInit {

  logs: any[] = [];
  loading = true;
  error = '';
  groupId!: number;

  constructor(
    public service: StudyGroupService,
    private route: ActivatedRoute,
    public router: Router
  ) {}

  ngOnInit(): void {
    const raw = this.route.snapshot.paramMap.get('id');

    if (!raw) {
      this.error = 'Aucun groupe sélectionné.';
      this.loading = false;
      return;
    }

    this.groupId = Number(raw);

    this.service.getAuditLog(this.groupId).subscribe({
      next: (data: any[]) => {

        // 🔥 TRI DU PLUS ANCIEN AU PLUS RÉCENT
        this.logs = data.sort((a, b) =>
          new Date(a.revisionDate).getTime() - new Date(b.revisionDate).getTime()
        );

        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement.';
        this.loading = false;
      }
    });
  }

  // 🔥 CALCUL DES CHANGEMENTS (DIFF)
  getChanges(current: any, previous: any) {
    if (!previous) return [];

    const changes: any[] = [];

    Object.keys(current).forEach(key => {
      if (current[key] !== previous[key]) {
        changes.push({
          field: key,
          oldValue: previous[key],
          newValue: current[key]
        });
      }
    });

    return changes;
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleString('fr-FR');
  }

  goBack(): void {
    this.router.navigate(['/study-groups']);
  }
}
