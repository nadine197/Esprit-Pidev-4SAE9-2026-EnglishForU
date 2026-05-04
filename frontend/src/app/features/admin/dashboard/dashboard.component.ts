import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  // Mock data for the view - You can replace these with API calls to your User microservice
  stats = [
    { label: 'Total Students', value: '1,284', icon: '👥', color: 'bg-blue-50', text: 'text-[#0066FF]' },
    { label: 'Active Tutors', value: '32', icon: '⭐', color: 'bg-green-50', text: 'text-green-600' },
    { label: 'Total Courses', value: '45', icon: '📚', color: 'bg-purple-50', text: 'text-purple-600' }
  ];

  recentUsers = [
    { name: 'Khalil Test', email: 'khalil@gmail.com', role: 'STUDENT', status: 'Active', date: '2026-02-19' },
    { name: 'Nadine Admin', email: 'nadine@lingua.com', role: 'ADMIN', status: 'Active', date: '2026-02-18' },
    { name: 'John Doe', email: 'john@example.com', role: 'STUDENT', status: 'Blocked', date: '2026-02-17' },
    { name: 'Ahmed Tutor', email: 'ahmed@lingua.com', role: 'TUTOR', status: 'Active', date: '2026-02-16' }
  ];

  constructor() { }

  ngOnInit(): void {
    console.log('Admin Dashboard Initialized');
  }

  // Placeholder for action buttons
  editUser(user: any) {
    console.log('Editing user:', user.name);
  }
}