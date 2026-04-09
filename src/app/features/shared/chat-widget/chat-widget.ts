import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DiscussionService } from '../../../services/discussion.service';
import { ChatService } from '../../../services/chat.service'; 
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat-widget',
  templateUrl: './chat-widget.html',
  styleUrls: ['./chat-widget.css']
})
export class ChatWidgetComponent implements OnInit, OnDestroy {
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;

  isOpen = false;
  unreadCount = 0;
  groups: any[] = []; 
  currentUser: any;
  selectedGroup: any = null; 
  messages: any[] = [];      
  newMessage: string = '';
  
  replyingTo: any = null; 
  isEditing = false;
  editingMsgId: string | null = null;
  
  typingUser: string | null = null;
  typingTimer: any;
  
  private messageSub?: Subscription;
  private typingSub?: Subscription;

  constructor(
    private discussionService: DiscussionService,
    private chatService: ChatService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      this.currentUser = JSON.parse(userJson);
      this.loadMyDiscussions();
    }
  }

  ngOnDestroy() { this.cleanup(); }

  // --- INITIALISATION & NOTIFS ---
  loadMyDiscussions() {
    if (!this.currentUser) return;
    const email = this.currentUser.email;
    const obs = this.currentUser.role === 'ADMIN' ? this.discussionService.getAllGroups() : this.discussionService.getMyGroupsByEmail(email);
    obs.subscribe({ next: (data: any) => {
      this.groups = data.content ? data.content : data;
      this.initBackgroundListening();
    }});
  }

  async initBackgroundListening() {
    try {
      await this.chatService.connect();
      this.groups.forEach(g => this.chatService.subscribeToGroup(g.id));
      if (this.messageSub) this.messageSub.unsubscribe();
      this.messageSub = this.chatService.messages$.subscribe(msg => this.handleIncomingSocketData(msg));
    } catch (err) { console.error("Socket error", err); }
  }

  handleIncomingSocketData(incomingMsg: any) {
    if (this.selectedGroup && incomingMsg.groupId === this.selectedGroup.id) {
      const index = this.messages.findIndex(m => m.id === incomingMsg.id);
      if (incomingMsg.content === "DELETED_SIGNAL") {
        if (index !== -1) this.messages.splice(index, 1);
      } else if (index !== -1) {
        this.messages[index] = { ...this.messages[index], ...incomingMsg };
      } else {
        this.messages.push(incomingMsg);
        this.scrollToBottom();
      }
    } else if (incomingMsg.senderId !== this.currentUser.email && incomingMsg.content !== "DELETED_SIGNAL") {
      this.unreadCount++;
      this.playNotificationSound();
    }
  }

  // --- ACTIONS ---
  selectGroup(group: any) {
    this.selectedGroup = group;
    this.messages = [];
    this.unreadCount = 0; 
    this.cancelReply();
    this.cancelEdit();
    this.chatService.getMessages(group.id).subscribe(data => {
      this.messages = data;
      this.scrollToBottom();
    });
    if (this.typingSub) this.typingSub.unsubscribe();
    this.typingSub = this.chatService.subscribeToTyping(group.id).subscribe((data: any) => {
      if (data.userName !== this.currentUser.name) this.typingUser = data.isTyping ? data.userName : null;
    });
  }


  onTyping() {
    if (!this.selectedGroup) return;
      console.log("Envoi du signal 'typing' pour :", this.currentUser.name); // <--- AJOUTE ÇA
    this.chatService.sendTypingStatus(this.selectedGroup.id, this.currentUser.name, true);
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      this.chatService.sendTypingStatus(this.selectedGroup.id, this.currentUser.name, false);
    }, 2000);
  }

  sendMsg() {
    if (!this.newMessage.trim() || !this.selectedGroup) return;
    if (this.isEditing) {
      const msg = this.messages.find(m => m.id === this.editingMsgId);
      this.chatService.editMessage(this.selectedGroup.id, { ...msg, content: this.newMessage });
      this.cancelEdit();
    } else {
      const chatMsg: any = {
        groupId: this.selectedGroup.id, senderId: this.currentUser.email,
        senderName: this.currentUser.name, content: this.newMessage, timestamp: new Date()
      };
      if (this.replyingTo) {
        chatMsg.replyToId = this.replyingTo.id; chatMsg.replyToText = this.replyingTo.content; chatMsg.replyToUser = this.replyingTo.senderName;
      }
      this.chatService.sendMessage(this.selectedGroup.id, chatMsg);
      this.newMessage = ''; this.cancelReply();
      this.chatService.sendTypingStatus(this.selectedGroup.id, this.currentUser.name, false);
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && this.selectedGroup) {
      const formData = new FormData();
      formData.append('file', file);
      this.http.post('http://localhost:8090/api/discussions/files/upload', formData, { responseType: 'text' })
        .subscribe(url => {
          const msg = { groupId: this.selectedGroup.id, senderId: this.currentUser.email, senderName: this.currentUser.name,
            content: "📁 Sent a file: " + file.name, fileUrl: url, fileName: file.name, timestamp: new Date()
          };
          this.chatService.sendMessage(this.selectedGroup.id, msg);
        });
    }
  }

  setupReply(m: any) { this.replyingTo = m; this.isEditing = false; }
  cancelReply() { this.replyingTo = null; }
  setupEdit(m: any) { this.isEditing = true; this.editingMsgId = m.id; this.newMessage = m.content; this.replyingTo = null; }
  cancelEdit() { this.isEditing = false; this.newMessage = ''; }
  onDeleteMsg(m: any) { if (confirm("Delete?")) this.chatService.deleteMessage(this.selectedGroup.id, m); }
  reactToMessage(m: any, e: string) { this.chatService.editMessage(this.selectedGroup.id, { ...m, reaction: e }); }
  toggleChat() { this.isOpen = !this.isOpen; if (this.isOpen) this.unreadCount = 0; }
  goBack() { this.selectedGroup = null; this.typingUser = null; }
  private cleanup() { this.chatService.disconnect(); if (this.messageSub) this.messageSub.unsubscribe(); if (this.typingSub) this.typingSub.unsubscribe(); }
  playNotificationSound() { new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3').play().catch(() => {}); }
  scrollToBottom() { setTimeout(() => { if (this.myScrollContainer) this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight; }, 100); }
}