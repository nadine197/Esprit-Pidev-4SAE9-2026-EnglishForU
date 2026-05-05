import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'EnglishForU';
  chatMode: 'none' | 'selector' | 'ai' | 'group' = 'none';

  constructor(private router: Router) {}

  ngOnInit() {}

  isUserConnected(): boolean {
    const user = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    return user !== null && token !== null;
  }

  toggleChat() {
    if (this.chatMode === 'none') {
      this.chatMode = 'selector';
    } else {
      this.chatMode = 'none';
    }
  }

  selectChat(mode: 'ai' | 'group') {
    this.chatMode = mode;
  }

  backToSelector() {
    this.chatMode = 'selector';
  }
}
