import { Component, OnInit } from '@angular/core';
import { DiscussionService } from 'src/app/services/discussion.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-discussion-mgmt',
  templateUrl: './discussion-mgmt.html'
})
export class DiscussionMgmtComponent implements OnInit {
  groups: any[] = [];
  tutors: any[] = [];
  students: any[] = [];
  currentUser: any;

  showModal = false;
  isEditMode = false;
  editingGroupId: string | null = null;

  newGroup = {
    groupName: '',
    tutorId: '',
    tutorName: '',
    studentIds: [] as string[]
  };

  constructor(
    private discussionService: DiscussionService,
    private userService: UserService
  ) {}

  ngOnInit() {
    // 1. Récupérer l'utilisateur connecté
    const userJson = localStorage.getItem('user');
    if (userJson) {
      this.currentUser = JSON.parse(userJson);
    }

    this.loadGroups();
    this.loadUsers();
  }

  // Charge les groupes (Tous pour Admin, filtrés pour les autres)
  loadGroups() {
    if (!this.currentUser) return;

    if (this.currentUser.role === 'ADMIN') {
      this.discussionService.getAllGroups().subscribe({
        next: (data: any) => this.groups = data.content ? data.content : data,
        error: (err) => console.error("Erreur chargement groupes", err)
      });
    } else {
      this.discussionService.getMyGroups(this.currentUser.id).subscribe({
        next: (data: any) => this.groups = data.content ? data.content : data,
        error: (err) => console.error("Erreur chargement mes groupes", err)
      });
    }
  }

  // Charge les tuteurs et étudiants pour le modal de création
  loadUsers() {
    this.userService.getAllTutors().subscribe({
      next: (res: any) => this.tutors = res.content ? res.content : res,
      error: (err) => console.error("Erreur tuteurs", err)
    });

    this.userService.getAllStudents().subscribe({
      next: (res: any) => this.students = res.content ? res.content : res,
      error: (err) => console.error("Erreur étudiants", err)
    });
  }

  // --- GESTION DU MODAL ---

  openAddModal() {
    this.isEditMode = false;
    this.editingGroupId = null;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(group: any) {
    this.isEditMode = true;
    this.editingGroupId = group.id;
    this.newGroup = {
      groupName: group.groupName,
      tutorId: group.tutorId,
      tutorName: group.tutorName,
      studentIds: [...group.studentIds] 
    };
    this.showModal = true;
  }

  resetForm() {
    this.newGroup = { groupName: '', tutorId: '', tutorName: '', studentIds: [] };
  }

  // --- ACTIONS FORMULAIRE ---

  toggleStudent(studentId: string) {
    const index = this.newGroup.studentIds.indexOf(studentId);
    if (index > -1) {
      this.newGroup.studentIds.splice(index, 1);
    } else {
      this.newGroup.studentIds.push(studentId);
    }
  }

  isStudentSelected(studentId: string): boolean {
    return this.newGroup.studentIds.includes(studentId);
  }

  onSaveGroup() {
    const selectedTutor = this.tutors.find(t => t.id === this.newGroup.tutorId);
    if (selectedTutor) {
      this.newGroup.tutorName = selectedTutor.name + ' ' + selectedTutor.lastName;
    }

    if (this.isEditMode && this.editingGroupId) {
      this.discussionService.updateGroup(this.editingGroupId, this.newGroup).subscribe({
        next: () => {
          this.loadGroups();
          this.showModal = false;
        }
      });
    } else {
      this.discussionService.createGroup(this.newGroup).subscribe({
        next: () => {
          this.loadGroups();
          this.showModal = false;
          this.resetForm();
        }
      });
    }
  }

  onDelete(id: string) {
    if (confirm("Permanently delete this group?")) {
      this.discussionService.deleteGroup(id).subscribe(() => this.loadGroups());
    }
  }
}