import { Component, OnInit } from '@angular/core';
import { DiscussionService } from 'src/app/services/discussion.service';
import { UserService } from 'src/app/services/user.service';
import { CourseService } from 'src/app/services/course.service';

@Component({
  selector: 'app-discussion-mgmt',
  templateUrl: './discussion-mgmt.html'
})
export class DiscussionMgmtComponent implements OnInit {
  groups: any[] = [];
  tutors: any[] = [];
  students: any[] = [];
  studyGroups: any[] = []; 
  currentUser: any;

  showModal = false;
  isEditMode = false;
  editingGroupId: string | null = null;
  selectedStudyGroupId: string = ''; // ID pour l'import rapide

  newGroup = {
    groupName: '',
    tutorEmail: '', 
    tutorName: '',
    studentEmails: [] as string[] 
  };

  constructor(
    private discussionService: DiscussionService,
    private userService: UserService,
    private courseService: CourseService 
  ) {}

  ngOnInit() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      this.currentUser = JSON.parse(userJson);
    }
    this.loadGroups();
    this.loadUsers();
    this.loadStudyGroups(); // Charger les groupes de cours pour l'import
  }

  // --- CHARGEMENT DES DONNÉES ---

  loadGroups() {
    if (!this.currentUser) return;
    const obs = this.currentUser.role === 'ADMIN' 
      ? this.discussionService.getAllGroups() 
      : this.discussionService.getMyGroupsByEmail(this.currentUser.email);

    obs.subscribe({
      next: (data: any) => this.groups = data.content ? data.content : data,
      error: (err) => console.error("Erreur chargement groupes", err)
    });
  }

  loadUsers() {
    // On ajoute explicitement (res: any) pour autoriser la lecture de .content
    this.userService.getAllTutors().subscribe((res: any) => {
      this.tutors = res && res.content ? res.content : res;
      console.log("Tuteurs chargés :", this.tutors);
    });

    this.userService.getAllStudents().subscribe((res: any) => {
      this.students = res && res.content ? res.content : res;
      console.log("Étudiants chargés :", this.students);
    });
  }

  loadStudyGroups() {
    this.courseService.getStudyGroups().subscribe((res: any) => {
      // Même logique ici pour l'import des groupes de cours
      this.studyGroups = res && res.content ? res.content : res;
      console.log("Study Groups chargés :", this.studyGroups);
    });
  }

  // --- LOGIQUE D'IMPORTATION RAPIDE ---

  onImportFromStudyGroup() {
    if (!this.selectedStudyGroupId) return;

    this.discussionService.createFromStudyGroup(this.selectedStudyGroupId).subscribe({
      next: () => {
        this.loadGroups(); // Rafraîchir la liste
        this.showModal = false;
        this.selectedStudyGroupId = '';
        console.log("Discussion synchronisée avec succès !");
      },
      error: (err) => alert("Error during import. Check if services are linked.")
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
    this.selectedStudyGroupId = '';
  }

  // --- ACTIONS FORMULAIRE MANUEL ---

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
    }

    const request = (this.isEditMode && this.editingGroupId)
      ? this.discussionService.updateGroup(this.editingGroupId, this.newGroup)
      : this.discussionService.createGroup(this.newGroup);

    request.subscribe({
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