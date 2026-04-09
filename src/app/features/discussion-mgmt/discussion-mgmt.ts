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

  // On utilise désormais les EMAILS pour identifier les membres
  newGroup = {
    groupName: '',
    tutorEmail: '', 
    tutorName: '',
    studentEmails: [] as string[] 
  };

  constructor(
    private discussionService: DiscussionService,
    private userService: UserService
  ) {}

  ngOnInit() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      this.currentUser = JSON.parse(userJson);
    }

    this.loadGroups();
    this.loadUsers();
  }

  // --- CHARGEMENT DES DONNÉES ---

  loadGroups() {
    if (!this.currentUser) return;

    if (this.currentUser.role === 'ADMIN') {
      this.discussionService.getAllGroups().subscribe({
        next: (data: any) => this.groups = data.content ? data.content : data,
        error: (err) => console.error("Erreur chargement groupes", err)
      });
    } else {
      // FIX : On filtre par EMAIL car l'ID est manquant dans le localStorage
      this.discussionService.getMyGroups(this.currentUser.email).subscribe({
        next: (data: any) => this.groups = data.content ? data.content : data,
        error: (err) => console.error("Erreur chargement mes groupes", err)
      });
    }
  }

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
      tutorEmail: group.tutorEmail,
      tutorName: group.tutorName,
      studentEmails: [...group.studentEmails] 
    };
    this.showModal = true;
  }

  resetForm() {
    this.newGroup = { groupName: '', tutorEmail: '', tutorName: '', studentEmails: [] };
  }

  // --- ACTIONS FORMULAIRE ---

  toggleStudent(student: any) {
    const email = student.email;
    const index = this.newGroup.studentEmails.indexOf(email);
    if (index > -1) {
      this.newGroup.studentEmails.splice(index, 1);
    } else {
      this.newGroup.studentEmails.push(email);
    }
  }

  isStudentSelected(student: any): boolean {
    return this.newGroup.studentEmails.includes(student.email);
  }

  onSaveGroup() {
    const selectedTutor = this.tutors.find(t => t.email === this.newGroup.tutorEmail);
    if (selectedTutor) {
      this.newGroup.tutorName = selectedTutor.name + ' ' + selectedTutor.lastName;
      // newGroup.tutorEmail est déjà rempli par le [(ngModel)] du select
    }

    this.discussionService.createGroup(this.newGroup).subscribe({
      next: () => {
        this.loadGroups();
        this.showModal = false;
      }
    });
}
  onDelete(id: string) {
    if (confirm("Permanently delete this group?")) {
      this.discussionService.deleteGroup(id).subscribe(() => this.loadGroups());
    }
  }
}