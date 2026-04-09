import { Component, OnInit } from '@angular/core';
<<<<<<< HEAD
import { CourseService } from 'src/app/services/course.service';
import { Course } from 'src/app/features/courses/models/courses';
=======

>>>>>>> 21f8a6f (metier avancer + controle de saisie)
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  // Mock data for the view - You can replace these with API calls to your User microservice
  stats = [
    { label: 'Total Students', value: '1,284', icon: 'users', color: 'bg-blue-50', text: 'text-[#0066FF]' },
    { label: 'Active Tutors', value: '32', icon: 'star', color: 'bg-green-50', text: 'text-green-600' },
    { label: 'Total Courses', value: '45', icon: 'book', color: 'bg-purple-50', text: 'text-purple-600' }
  ];

  recentUsers = [
    { name: 'Khalil Test', email: 'khalil@gmail.com', role: 'STUDENT', status: 'Active', date: '2026-02-19' },
    { name: 'Nadine Admin', email: 'nadine@lingua.com', role: 'ADMIN', status: 'Active', date: '2026-02-18' },
    { name: 'John Doe', email: 'john@example.com', role: 'STUDENT', status: 'Blocked', date: '2026-02-17' },
    { name: 'Ahmed Tutor', email: 'ahmed@lingua.com', role: 'TUTOR', status: 'Active', date: '2026-02-16' }
  ];
<<<<<<< HEAD
  courses: Course[] = [];
  totalCourses = 0;
  loading = true;
  error = '';
  constructor(private courseService: CourseService) {}
  ngOnInit(): void {
    console.log('Admin Dashboard Initialized');
    this.loadCourses();
  }
  loadCourses() {
    this.loading = true;

    this.courseService.getAll().subscribe({
      next: (data) => {
        this.courses = data;
        this.totalCourses = data.length;

        // 🔥 Update stat card automatically
        this.stats[2].value = this.totalCourses.toString();

        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading courses:', err);
        this.error = 'Cannot load courses';
        this.loading = false;
      }
    });
  }
  deleteCourse(id?: number) {
    if (!id) return;

    if (!confirm('Delete this course ?')) return;

    this.courseService.delete(id).subscribe({
      next: () => {
        this.loadCourses(); // refresh dashboard
      },
      error: (err) => console.error(err)
    });
  }
  editUser(user: any) {
    console.log('Editing user:', user.name);
  }
}
=======

  constructor() { }

  ngOnInit(): void {
    console.log('Admin Dashboard Initialized');
  }

  // Placeholder for action buttons
  editUser(user: any) {
    console.log('Editing user:', user.name);
  }
}
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
