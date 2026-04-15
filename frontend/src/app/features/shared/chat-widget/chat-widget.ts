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

  // --- ÉTAT INTERFACE ---
  isOpen = false;
  unreadCount = 0;
  groups: any[] = []; 
  currentUser: any;
  selectedGroup: any = null; 

  // --- DONNÉES CHAT ---
  messages: any[] = [];      
  newMessage: string = '';
  pinnedMessage: any = null; 
  
  // --- ÉTATS AVANCÉS ---
  replyingTo: any = null; 
  isEditing = false;
  editingMsgId: string | null = null;
  typingUser: string | null = null;
  typingTimer: any;
  
  private messageSub?: Subscription;

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

  // --- 1. CHARGEMENT & NOTIFICATIONS ---
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
      // On s'abonne à tous les groupes pour les notifs en arrière-plan
      this.groups.forEach(g => this.chatService.subscribeToGroup(g.id));

      if (this.messageSub) this.messageSub.unsubscribe();
      this.messageSub = this.chatService.messages$.subscribe(msg => {
        this.handleIncomingSocketData(msg);
      });
    } catch (err) { console.error("WebSocket connection failed", err); }
  }

  handleIncomingSocketData(incomingMsg: any) {
  if (this.selectedGroup && incomingMsg.groupId === this.selectedGroup.id) {
    const index = this.messages.findIndex(m => m.id === incomingMsg.id);

    if (index !== -1) {
      // On met à jour le message dans la liste
      this.messages[index] = { ...this.messages[index], ...incomingMsg };
      
      // --- CETTE LOGIQUE EST CRUCIALE ---
      if (incomingMsg.isPinned) {
        this.pinnedMessage = incomingMsg; // Affiche la barre bleue
      } else if (this.pinnedMessage && this.pinnedMessage.id === incomingMsg.id) {
        this.pinnedMessage = null; // Cache la barre bleue si on a désépinglé CE message
      }
    } else {
      this.messages.push(incomingMsg);
      this.scrollToBottom();
    }
  }
}

  // --- 2. LOGIQUE DE SÉLECTION ---
  selectGroup(group: any) {
    this.selectedGroup = group;
    this.messages = [];
    this.unreadCount = 0; 
    this.pinnedMessage = null;
    this.cancelReply();
    this.cancelEdit();

    this.chatService.getMessages(group.id).subscribe(data => {
      this.messages = data;
  this.pinnedMessage = this.messages.find(m => m.isPinned === true);
      this.scrollToBottom();
    });

    this.chatService.subscribeToTyping(group.id).subscribe((data: any) => {
      if (data.userName !== this.currentUser.name) {
        this.typingUser = data.isTyping ? data.userName : null;
      }
    });
  }

  // --- 3. ACTIONS MESSAGES ---
  sendMsg() {
    if (!this.newMessage.trim() || !this.selectedGroup) return;

    if (this.isEditing) {
      const msg = this.messages.find(m => m.id === this.editingMsgId);
      this.chatService.editMessage(this.selectedGroup.id, { ...msg, content: this.newMessage });
      this.cancelEdit();
    } else {
      const chatMsg: any = {
        groupId: this.selectedGroup.id,
        senderId: this.currentUser.email,
        senderName: this.currentUser.name,
        content: this.newMessage,
        timestamp: new Date()
      };
      if (this.replyingTo) {
        chatMsg.replyToId = this.replyingTo.id;
        chatMsg.replyToText = this.replyingTo.content;
        chatMsg.replyToUser = this.replyingTo.senderName;
      }
      this.chatService.sendMessage(this.selectedGroup.id, chatMsg);
      this.newMessage = ''; 
      this.cancelReply();
    }
  }

  // Dans chat-widget.ts
onPin(msg: any) {
  console.log("Action Pin déclenchée sur :", msg.content);
  
  // 1. Inversion du statut
  const updatedMsg = { ...msg, isPinned: !msg.isPinned };
  
  // 2. Mise à jour LOCALE immédiate pour que l'admin voie le résultat sans attendre le serveur
  if (updatedMsg.isPinned) {
    this.pinnedMessage = updatedMsg;
  } else {
    this.pinnedMessage = null;
  }

  // 3. Envoi au serveur
  this.chatService.editMessage(this.selectedGroup.id, updatedMsg);
}

  onDeleteMsg(msg: any) {
    if (confirm("Delete this message?")) this.chatService.deleteMessage(this.selectedGroup.id, msg);
  }

  reactToMessage(msg: any, emoji: string) {
    this.chatService.editMessage(this.selectedGroup.id, { ...msg, reaction: emoji });
  }

  // --- 4. FICHIERS & TYPING ---
  onTyping() {
    if (!this.selectedGroup) return;
    this.chatService.sendTypingStatus(this.selectedGroup.id, this.currentUser.name, true);
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      this.chatService.sendTypingStatus(this.selectedGroup.id, this.currentUser.name, false);
    }, 2000);
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && this.selectedGroup) {
      const formData = new FormData();
      formData.append('file', file);
      this.http.post('http://localhost:8090/api/discussions/files/upload', formData, { responseType: 'text' })
        .subscribe(url => {
          const msg = { 
            groupId: this.selectedGroup.id, senderId: this.currentUser.email, senderName: this.currentUser.name,
            content: "📁 Sent a file: " + file.name, fileUrl: url, fileName: file.name, timestamp: new Date()
          };
          this.chatService.sendMessage(this.selectedGroup.id, msg);
        });
    }
  }

  // --- 5. UI UTILS ---
  setupReply(m: any) { this.replyingTo = m; this.isEditing = false; }
  cancelReply() { this.replyingTo = null; }
  setupEdit(m: any) { this.isEditing = true; this.editingMsgId = m.id; this.newMessage = m.content; this.replyingTo = null; }
  cancelEdit() { this.isEditing = false; this.editingMsgId = null; this.newMessage = ''; }
  toggleChat() { this.isOpen = !this.isOpen; if (this.isOpen) this.unreadCount = 0; }
  goBack() { this.selectedGroup = null; this.typingUser = null; }
  private cleanup() { this.chatService.disconnect(); }
  playNotificationSound() { new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3').play().catch(() => {}); }
  scrollToBottom() { setTimeout(() => { if (this.myScrollContainer) this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight; }, 100); }
}