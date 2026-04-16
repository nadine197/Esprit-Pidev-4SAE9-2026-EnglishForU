import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.html'
})
export class UserListComponent implements OnInit {
  users: any[] = [];
  currentRole: string = 'all';
  loading: boolean = false;

  showModal: boolean = false;
  isEditMode: boolean = false;
  userForm: FormGroup;
  selectedUserId: string | null = null;

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.userForm = this.fb.group({
      name: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      password: [''], // Sera rendu obligatoire dynamiquement pour l'ajout
      role: ['STUDENT', Validators.required]
    });
  }

  ngOnInit(): void {
    // Écoute les changements de paramètres dans l'URL (/admin/users/STUDENT par exemple)
    this.route.params.subscribe(params => {
      this.currentRole = params['role'] || 'all';
      this.loadData();
    });
  }

  loadData() {
  this.loading = true;
  if (this.currentRole === 'STUDENT') {
    this.userService.getAllStudents().subscribe((data: any[]) => {
      this.users = data;
      this.loading = false;
    });
  } else if (this.currentRole === 'TUTOR') {
    this.userService.getAllTutors().subscribe((data: any[]) => {
      this.users = data;
      this.loading = false;
    });
  } else {
    // Ici, on n'appellera que les ADMINS
    this.userService.getAllAdmins().subscribe((data: any[]) => {
      this.users = data;
      this.loading = false;
    });
  }
}

  // --- Méthodes CRUD (Fixent les erreurs de clic dans le HTML) ---

  openAddModal() {
    this.isEditMode = false;
    this.selectedUserId = null;
    this.userForm.reset({ role: 'STUDENT' });
    
    // Pour un nouvel utilisateur, le mot de passe est requis
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    
    this.showModal = true;
  }

  openEditModal(user: any) {
    this.isEditMode = true;
    this.selectedUserId = user.id;
    
    // On remplit le formulaire avec les données de l'utilisateur
    this.userForm.patchValue({
      name: user.name,
      lastName: user.lastName,
      email: user.email,
      phone: user.phone,
      role: user.role
    });

    // Pour une modification, le mot de passe n'est pas obligatoire
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    
    this.showModal = true;
  }

  saveUser() {
    if (this.userForm.invalid) return;

    const userData = this.userForm.value;

    if (this.isEditMode && this.selectedUserId) {
      // Cas de la mise à jour
      this.userService.updateUser(this.selectedUserId, userData).subscribe({
        next: () => {
          this.loadData();
          this.showModal = false;
        }
      });
    } else {
      // Cas de la création
      this.userService.createUser(userData).subscribe({
        next: () => {
          this.loadData();
          this.showModal = false;
        }
      });
    }
  }

  toggleBlock(user: any) {
    const action = user.active ? this.userService.blockUser(user.id) : this.userService.unblockUser(user.id);
    action.subscribe({
      next: () => {
        user.active = !user.active; // Mise à jour visuelle immédiate
      }
    });
  }

  deleteUser(id: string) {
    if (confirm('Are you sure you want to delete this user?')) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.loadData();
        }
      });
    }
  }
}