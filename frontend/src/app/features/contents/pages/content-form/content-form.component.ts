import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ContentService } from '../../../../services/content.service';
import { Content } from '../../models/content';

@Component({
  selector: 'app-content-form',
  templateUrl: './content-form.component.html',
  styleUrls: ['./content-form.component.css']
})
export class ContentFormComponent implements OnInit {

  contentForm!: FormGroup;
  isEditMode = false;
  contentId!: number;

  constructor(
    private fb: FormBuilder,
    private service: ContentService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.contentForm = this.fb.group({
      title: ['', Validators.required],
      type: ['', Validators.required],
      url: ['', Validators.required],
      courseId: [0, Validators.required],
      authorId: ['', Validators.required]
    });

    // check edit mode
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.contentId = Number(id);
      this.loadContent(this.contentId);
    }
  }

  loadContent(id: number) {
    this.service.getById(id).subscribe({
      next: (content) => {
        this.contentForm.patchValue(content);
      },
      error: () => alert('Content not found')
    });
  }

  onSubmit() {

    if (this.contentForm.invalid) return;

    const content: Content = {
      contentId: this.contentId,
      ...this.contentForm.value
    };

    if (this.isEditMode) {
      this.service.update(this.contentId, content).subscribe(() => {
        alert('Content updated!');
        this.router.navigate(['/contents']);
      });
    } else {
      this.service.create(content).subscribe(() => {
        alert('Content created!');
        this.router.navigate(['/contents']);
      });
    }
  }
}
