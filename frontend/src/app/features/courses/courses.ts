import { Component, OnInit } from '@angular/core';
import { CourseService } from 'src/app/services/course.service';
import { Course } from './models/courses';

@Component({
  selector: 'app-courses',
  templateUrl: './courses.html'
})
export class CoursesComponent implements OnInit {
  categories = ['All', 'General English', 'IELTS Prep', 'Business English', 'Kids & Teens'];
  selectedCategory = 'All';

  courses: any[] = [];
  isLoading = true;

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses() {
    this.isLoading = true;
    this.courseService.getAll().subscribe({
      next: (data: Course[]) => {
        this.courses = data.map(c => ({
          courseid: c.courseid,
          title: c.title,
          desc: c.description,
          duration: `${c.duration} Weeks`, // Backend uses Integer, template expects string like '8 Weeks'
          level: 'B1 - B2', // Default level
          price: 'FREE',    // Default price
          image: this.getRandomEmoji(c.title),
          category: 'General English'
        }));
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading courses', err);
        this.isLoading = false;
      }
    });
  }

  getRandomEmoji(title: string): string {
    const emojis = ['📚', '🎓', '💼', '🗣️', '📝', '🌟'];
    const index = Math.abs(this.hashCode(title)) % emojis.length;
    return emojis[index];
  }

  private hashCode(s: string): number {
    let h = 0;
    for(let i = 0; i < s.length; i++)
        h = Math.imul(31, h) + s.charCodeAt(i) | 0;
    return h;
  }
}