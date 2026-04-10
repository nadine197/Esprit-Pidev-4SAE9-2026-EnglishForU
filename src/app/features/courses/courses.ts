import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Course, CourseService } from 'src/app/services/courses.service';


@Component({
  selector: 'app-courses',
  templateUrl: './courses.html'
})
export class CoursesComponent implements OnInit {
  categories = ['All', 'General English', 'IELTS Prep', 'Business English', 'Kids & Teens'];
  selectedCategory = 'All';
  courses: Course[] = [];
 

  constructor(private courseService: CourseService, private router: Router) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses() {
    this.courseService.getAllCourses().subscribe({
      next: (data) => {
        // Adapter la réponse au design existant
        this.courses = data.map(c => ({
          ...c,
          desc: c.description,
          image: '📚', 
          duration: c.duration + 'h'
        }));
      },
      error: (err) => {
        console.error('Erreur lors du chargement des cours :', err);
      }
    });
  }
goToDetails(courseId: number) {
  this.router.navigate(['/coursesDetails', courseId]);
}

}