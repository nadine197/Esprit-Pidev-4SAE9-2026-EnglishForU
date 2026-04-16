import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.html'
})
export class UserListComponent implements OnInit {
  users: any[] = [];
  loading = false;
  showModal = false;
  userForm: FormGroup;
  isEditMode = false;
  selectedUserId: string | null = null;

  constructor(private userService: UserService, private fb: FormBuilder) {
    this.userForm = this.fb.group({
      name: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      password: [''],
      role: ['STUDENT', Validators.required]
    });
  }

  ngOnInit(): void { this.loadUsers(); }

  loadUsers() {
    this.loading = true;
    this.userService.getAllUsers().subscribe(data => {
      this.users = data;
      this.loading = false;
    });
  }

  openAddModal() {
    this.isEditMode = false;
    this.userForm.reset({ role: 'STUDENT' });
    this.showModal = true;
  }

  openEditModal(user: any) {
    this.isEditMode = true;
    this.selectedUserId = user.id;
    this.userForm.patchValue(user);
    this.showModal = true;
  }

  saveUser() {
    if (this.userForm.valid) {
      const userData = this.userForm.value;
      if (this.isEditMode) {
        this.userService.updateUser(this.selectedUserId!, userData).subscribe(() => {
          this.loadUsers();
          this.showModal = false;
        });
      } else {
        this.userService.createUser(userData).subscribe(() => {
          this.loadUsers();
          this.showModal = false;
        });
      }
    }
  }

  deleteUser(id: string) {
    if(confirm('Are you sure you want to delete this user?')) {
      this.userService.deleteUser(id).subscribe(() => this.loadUsers());
    }
  }

  toggleBlock(user: any) {
    const action = user.active ? this.userService.blockUser(user.id) : this.userService.unblockUser(user.id);
    action.subscribe(() => user.active = !user.active);
  }
}
