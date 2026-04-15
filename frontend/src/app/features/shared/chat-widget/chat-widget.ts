import { Component, OnInit, OnDestroy, ViewChild, ElementRef, HostListener } from '@angular/core';
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
  
  // --- ÉTATS AVANCÉS (REPLY / EDIT / TYPING) ---
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

  ngOnDestroy() { 
    this.cleanup(); 
  }

  // --- 1. CHARGEMENT INITIAL & ÉCOUTE GLOBALE ---

  loadMyDiscussions() {
    if (!this.currentUser) return;
    const email = this.currentUser.email;
    
    // On choisit l'observable selon le rôle
    const obs = this.currentUser.role === 'ADMIN' 
      ? this.discussionService.getAllGroups() 
      : this.discussionService.getMyGroupsByEmail(email);

    obs.subscribe({ 
      next: (data: any) => {
        this.groups = data.content ? data.content : data;
        // On initialise la connexion WebSocket pour les notifications
        this.initNotificationSystem();
      },
      error: (err: any) => console.error("Erreur chargement groupes", err)
    });
  }

  async initNotificationSystem() {
    try {
      await this.chatService.connect();
      // On s'abonne à tous les groupes de l'utilisateur pour le badge rouge
      this.groups.forEach(g => this.chatService.subscribeToGroup(g.id));

      if (this.messageSub) this.messageSub.unsubscribe();
      this.messageSub = this.chatService.messages$.subscribe(msg => {
        this.handleIncomingSocketData(msg);
      });
    } catch (err) {
      console.error("Erreur d'initialisation WebSocket", err);
    }
  }

  handleIncomingSocketData(incomingMsg: any) {
    // CAS A : Le message appartient au groupe ouvert à l'écran
    if (this.selectedGroup && incomingMsg.groupId === this.selectedGroup.id) {
      const index = this.messages.findIndex(m => m.id === incomingMsg.id);

      if (incomingMsg.content === "DELETED_SIGNAL") {
        if (index !== -1) this.messages.splice(index, 1);
        if (this.pinnedMessage?.id === incomingMsg.id) this.pinnedMessage = null;
      } 
      else if (index !== -1) {
        // Mise à jour (Edit, Réaction ou Pin)
        this.messages[index] = { ...this.messages[index], ...incomingMsg };
        if (incomingMsg.isPinned) this.pinnedMessage = incomingMsg;
        else if (this.pinnedMessage?.id === incomingMsg.id) this.pinnedMessage = null;
      } 
      else {
        // Nouveau message
        this.messages.push(incomingMsg);
        this.scrollToBottom();
      }
    } 
    // CAS B : Notification (Badge rouge)
    else {
      // Si ce n'est pas mon propre message, on incrémente le compteur
      if (incomingMsg.senderId !== this.currentUser.email && incomingMsg.content !== "DELETED_SIGNAL") {
        this.unreadCount++;
        this.playNotificationSound();
      }
    }
  }

  // --- 2. LOGIQUE DE SÉLECTION D'UN GROUPE ---

  selectGroup(group: any) {
    this.selectedGroup = group;
    this.messages = [];
    this.unreadCount = 0; 
    this.pinnedMessage = null;
    this.cancelReply();
    this.cancelEdit();
    this.typingUser = null;

    // Charger l'historique via REST (Gateway 8090)
    this.chatService.getMessages(group.id).subscribe({
      next: (data: any) => {
        this.messages = Array.isArray(data) ? data : (data.content || []);
        this.pinnedMessage = this.messages.find(m => m.isPinned);
        this.scrollToBottom();
      }
    });

    // Écouter le statut "Typing" spécifique à ce groupe
    if (this.typingSub) this.typingSub.unsubscribe();
    this.typingSub = this.chatService.subscribeToTyping(group.id).subscribe((data: any) => {
      if (data.userName !== this.currentUser.name) {
        this.typingUser = data.isTyping ? data.userName : null;
      }
    });
  }

  // --- 3. ENVOI & ACTIONS MESSAGES ---

  sendMsg() {
    if (!this.newMessage.trim() || !this.selectedGroup) return;

    if (this.isEditing) {
      // ACTION MODIFICATION
      const msg = this.messages.find(m => m.id === this.editingMsgId);
      if (msg) {
        this.chatService.editMessage(this.selectedGroup.id, { ...msg, content: this.newMessage });
      }
      this.cancelEdit();
    } else {
      // ACTION ENVOI NORMAL OU RÉPONSE
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
      // Arrêt immédiat du statut typing après envoi
      this.chatService.sendTypingStatus(this.selectedGroup.id, this.currentUser.name, false);
    }
  }

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

      // Upload via Gateway (8090)
      this.http.post('http://localhost:8090/api/discussions/files/upload', formData, { responseType: 'text' })
        .subscribe({
          next: (url) => {
            const msg = { 
              groupId: this.selectedGroup.id, 
              senderId: this.currentUser.email, 
              senderName: this.currentUser.name,
              content: "📁 Shared a file: " + file.name, 
              fileUrl: url, 
              fileName: file.name, 
              timestamp: new Date()
            };
            this.chatService.sendMessage(this.selectedGroup.id, msg);
          },
          error: (err: any) => alert("Upload failed. Check if /uploads folder exists on server.")
        });
    }
  }

  // --- 4. RÉACTIONS, PIN, DELETE ---

  reactToMessage(msg: any, emoji: string) {
    const updated = { ...msg, reaction: emoji };
    this.chatService.editMessage(this.selectedGroup.id, updated);
  }

  onPin(msg: any) {
    if (this.currentUser.role === 'STUDENT') return;
    const updated = { ...msg, isPinned: !msg.isPinned };
    this.chatService.pinMessage(this.selectedGroup.id, updated);
  }

  onDeleteMsg(msg: any) {
    if (confirm("Delete this message for everyone?")) {
      this.chatService.deleteMessage(this.selectedGroup.id, msg);
    }
  }

  // --- 5. UI UTILS & NAVIGATION ---

  setupReply(m: any) { this.isEditing = false; this.replyingTo = m; }
  cancelReply() { this.replyingTo = null; }
  
  setupEdit(m: any) { 
    this.replyingTo = null;
    this.isEditing = true; 
    this.editingMsgId = m.id; 
    this.newMessage = m.content; 
  }
  cancelEdit() { this.isEditing = false; this.editingMsgId = null; this.newMessage = ''; }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) this.unreadCount = 0;
  }

  goBack() {
    this.selectedGroup = null;
    this.typingUser = null;
    this.cancelEdit();
    this.cancelReply();
  }

  private cleanup() {
    this.chatService.disconnect();
    if (this.messageSub) this.messageSub.unsubscribe();
    if (this.typingSub) this.typingSub.unsubscribe();
  }

  playNotificationSound() {
    const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3');
    audio.play().catch(() => {});
  }

  scrollToBottom(): void {
    setTimeout(() => {
      if (this.myScrollContainer) {
        this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }
}