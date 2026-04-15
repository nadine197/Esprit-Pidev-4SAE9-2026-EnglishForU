import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService } from '../../../../services/course.service';
import { Course } from '../../models/courses';

@Component({
  selector: 'app-course-details',
  templateUrl: './course-details.component.html',
  styleUrls: ['./course-details.component.css']
})
export class CourseDetailsComponent implements OnInit {

  course?: Course;
  id!: number;

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCourse();
  }

  loadCourse() {
    this.courseService.getById(this.id).subscribe({
      next: (data: Course) => {
        this.course = data;
      },
      error: () => alert("Course not found")
    });
  }

  back() {
    this.router.navigate(['/courses']);
  }
}
