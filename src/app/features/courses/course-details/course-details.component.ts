import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CourseService, Course } from 'src/app/services/courses.service';

@Component({
  selector: 'app-course-details',
  templateUrl: './course-details.component.html',
  styleUrls: ['./course-details.component.css']
})
export class CourseDetailsComponent implements OnInit {

  course?: Course;
  loading = true;
 userRole : string='' ;
 courseId!: number;
  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService
  ) {}

  ngOnInit(): void {
  const storedRole = localStorage.getItem('ROLE');
  this.userRole = storedRole ? storedRole.replace(/"/g, '') : '';
    const id = this.route.snapshot.paramMap.get('id');
  this.courseId = Number(this.route.snapshot.paramMap.get('courseId'));
localStorage.setItem("courseId", this.courseId.toString());

    if (id) {
      this.courseService.getCourseById(+id).subscribe({
        next: (data) => {
          this.course = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Erreur chargement cours :', err);
          this.loading = false;
        }
      });
    }
  }
}