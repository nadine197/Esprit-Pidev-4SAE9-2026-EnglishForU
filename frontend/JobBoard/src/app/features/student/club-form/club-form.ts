import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubService } from '../../../services/club.service';
import { ImageUploadService } from '../../../services/image-upload.service';

@Component({
  selector: 'app-club-form',
  templateUrl: './club-form.html'
})
export class ClubFormComponent implements OnInit {
  clubForm: FormGroup;
  isEditMode = false;
  clubId: number | null = null;
  loading = false;
  saving = false;
  uploading = false;
  imageMode: 'url' | 'upload' = 'url';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private clubService: ClubService,
    private imageUploadService: ImageUploadService
  ) {
    this.clubForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      imageUrl: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.clubId = Number(id);
      this.loading = true;
      this.clubService.getClubById(this.clubId).subscribe({
        next: data => {
          this.clubForm.patchValue({
            name: data.name,
            description: data.description,
            imageUrl: data.imageUrl
          });
          this.loading = false;
        },
        error: () => { this.loading = false; }
      });
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      this.uploading = true;
      this.imageUploadService.uploadImage(file).subscribe({
        next: (url) => {
          this.clubForm.patchValue({ imageUrl: url });
          this.uploading = false;
        },
        error: () => { this.uploading = false; }
      });
    }
  }

  onSubmit() {
    if (this.clubForm.valid) {
      this.saving = true;
      const data = this.clubForm.value;

      if (this.isEditMode && this.clubId) {
        this.clubService.updateClub(this.clubId, data).subscribe({
          next: () => { this.router.navigate(['/student/clubs', this.clubId]); },
          error: () => { this.saving = false; }
        });
      } else {
        this.clubService.createClub(data).subscribe({
          next: (res: any) => { this.router.navigate(['/student/clubs', res.id]); },
          error: () => { this.saving = false; }
        });
      }
    }
  }
}

