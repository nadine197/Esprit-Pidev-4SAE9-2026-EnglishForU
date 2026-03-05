import { Component, OnInit } from '@angular/core';
import { DiscussionService } from '../../../services/discussion.service';

@Component({
  selector: 'app-chat-widget',
  templateUrl: './chat-widget.html',
})
export class ChatWidgetComponent implements OnInit {
  isOpen = false;
  groups: any[] = []; 
  currentUser: any;

  constructor(private discussionService: DiscussionService) {}

  ngOnInit() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      this.currentUser = JSON.parse(userJson);
      this.loadMyDiscussions();
    }
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  loadMyDiscussions() {
  const userJson = localStorage.getItem('user');
  if (!userJson) return;

  this.currentUser = JSON.parse(userJson);
  

  console.log("ID envoyé au serveur :", this.currentUser.id); 

  if (this.currentUser.role === 'ADMIN') {
    this.discussionService.getAllGroups().subscribe(data => this.groups = data);
  } else {
    this.discussionService.getMyGroups(this.currentUser.id.toString()).subscribe({
      next: (data: any) => {
        this.groups = data.content ? data.content : data;
        console.log("Groupes reçus :", this.groups);
      },
      error: (err) => console.error("Erreur API :", err)
    });
  }
}
}