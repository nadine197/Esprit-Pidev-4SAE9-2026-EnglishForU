import { Component, Input, ElementRef, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { AiChatService } from '../../../services/ai-chat.service';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat-widget',
  templateUrl: './ai-chat-widget.html'
})
export class AiChatWidgetComponent implements OnChanges {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  @Input() isOpen = false;
  messages: Message[] = [];
  userInput = '';
  isLoading = false;
  error = '';

  suggestions = [
    'What clubs can I join?',
    'Any upcoming events?',
    'Recommend something fun!',
    'How do I create a club?'
  ];

  constructor(private aiChat: AiChatService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isOpen']?.currentValue && this.messages.length === 0) {
      this.messages.push({
        role: 'assistant',
        content: "Hey! I'm LinguaBot, your AI assistant. I can help you find clubs, discover events, or navigate the app. What are you looking for?",
        timestamp: new Date()
      });
    }
  }

  sendMessage(text?: string) {
    const msg = text || this.userInput.trim();
    if (!msg || this.isLoading) return;

    this.messages.push({ role: 'user', content: msg, timestamp: new Date() });
    this.userInput = '';
    this.isLoading = true;
    this.error = '';
    this.scrollToBottom();

    this.aiChat.sendMessage(msg).subscribe({
      next: (reply) => {
        this.messages.push({ role: 'assistant', content: reply, timestamp: new Date() });
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: () => {
        this.error = 'Failed to get response. Try again.';
        this.isLoading = false;
        this.scrollToBottom();
      }
    });
  }

  clearChat() {
    this.messages = [];
    this.aiChat.clearHistory();
    this.messages.push({
      role: 'assistant',
      content: "Chat cleared! How can I help you?",
      timestamp: new Date()
    });
  }

  onKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom() {
    setTimeout(() => {
      if (this.messagesContainer) {
        const el = this.messagesContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    }, 50);
  }
}
