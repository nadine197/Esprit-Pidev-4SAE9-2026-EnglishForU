import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService } from 'src/app/services/course.service';
import { Course } from '../../models/courses';

@Component({
  selector: 'app-course-form',
  templateUrl: './course-form.component.html',
  styleUrls: ['./course-form.component.css']
})
export class CourseFormComponent implements OnInit {

  courseForm!: FormGroup;
  isEditMode = false;
  courseId!: number;

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {

    // 1️⃣ Create form
    this.courseForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      duration: [0, [Validators.required, Validators.min(1)]],
      adminId: ['', Validators.required]
    });

    // 2️⃣ Check if edit mode
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.courseId = +id;
      this.loadCourse(this.courseId);
    }
  }

  // Load course for edit
  loadCourse(id: number) {
    this.courseService.getById(id).subscribe(course => {
      this.courseForm.patchValue(course);
    });
  }

  // Submit form
  onSubmit() {
    if (this.courseForm.invalid) return;

    const course: Course = this.courseForm.value;

    if (this.isEditMode) {
      this.courseService.update(this.courseId, course).subscribe(() => {
        this.router.navigate(['/courses']);
      });
    } else {
      this.courseService.create(course).subscribe(() => {
        this.router.navigate(['/courses']);
      });
    }
  }
}
