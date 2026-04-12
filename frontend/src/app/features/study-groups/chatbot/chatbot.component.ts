import { Component } from '@angular/core';
import { StudyGroupService } from '../../../services/study-group.service';

interface Message {
  role: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent {

  // ✅ ouverture/fermeture chatbot
  isOpen = false;

  messages: Message[] = [
    { role: 'bot', text: "Bonjour ! Comment puis-je vous aider avec les groupes d'étude ?" }
  ];

  userInput = '';
  groupId?: number;
  loading = false;

  constructor(private studyGroupService: StudyGroupService) {}

  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  sendMessage(): void {
    const message = this.userInput.trim();
    if (!message) return;

    this.messages.push({ role: 'user', text: message });
    this.userInput = '';
    this.loading = true;

    this.studyGroupService.chat(message, this.groupId).subscribe({
      next: (res) => {
        this.messages.push({ role: 'bot', text: res.reply });
        this.loading = false;
      },
      error: () => {
        this.messages.push({ role: 'bot', text: 'Erreur de connexion au chatbot.' });
        this.loading = false;
      }
    });
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.sendMessage();
    }
  }
}
