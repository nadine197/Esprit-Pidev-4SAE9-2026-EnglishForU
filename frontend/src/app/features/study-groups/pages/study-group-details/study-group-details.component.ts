import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StudyGroupService } from '../../../../services/study-group.service';
import { StudyGroup } from '../../models/study-group';
import { StudyGroupNotificationService } from '../../../../services/study-group-notification.service';

@Component({
  selector: 'app-study-group-details',
  templateUrl: './study-group-details.component.html',
  styleUrls: ['./study-group-details.component.css']
})
export class StudyGroupDetailsComponent implements OnInit, OnDestroy {

  group!: StudyGroup;
  error = '';
  groupId = 0;

  // Pour afficher ou cacher la carte
  showMap = false;
  showChatbot = false;

  constructor(
    private route: ActivatedRoute,
    private service: StudyGroupService,
    private router: Router,
    private notifService: StudyGroupNotificationService
  ) {}

  ngOnInit(): void {
    const raw = this.route.snapshot.paramMap.get('id');

    if (!raw || isNaN(Number(raw))) {
      this.error = 'ID invalide.';
      this.router.navigate(['/study-groups']);
      return;
    }

    this.groupId = Number(raw);

    this.service.getById(this.groupId).subscribe({
      next: (data) => this.group = data,
      error: () => {
        this.error = 'Groupe introuvable.';
        this.router.navigate(['/study-groups']);
      }
    });

    this.notifService.connect();
    this.notifService.subscribeToGroup(this.groupId);
  }

  onContentAdded(title: string, type: string): void {
    this.notifService.sendNewContent(this.groupId, title, type);
  }

  onSessionPlanned(date: string): void {
    this.notifService.sendNewSession(this.groupId, date);
  }

  onMessageSent(sender: string): void {
    this.notifService.sendNewMessage(this.groupId, sender);
  }

  ngOnDestroy(): void {
    this.notifService.disconnect();
  }
}
