import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
  addQuizLink: any[] = [];
  viewQuizzesLink: any[] = [];
  private isAdminQuizContext = false;
  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private router: Router
  ) {}

  ngOnInit(): void {
  const storedRole = localStorage.getItem('ROLE');
  this.userRole = storedRole ? storedRole.replace(/"/g, '') : '';
    this.isAdminQuizContext = this.router.url.startsWith('/admin/quizzes');
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      const courseId = +id;
      this.addQuizLink = this.isAdminQuizContext
        ? ['/admin/quizzes/create', courseId]
        : ['/AddQuiz', courseId];
      this.viewQuizzesLink = this.isAdminQuizContext
        ? ['/admin/quizzes/list', courseId]
        : ['/quiz', courseId];

      this.courseService.getCourseById(courseId).subscribe({
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
