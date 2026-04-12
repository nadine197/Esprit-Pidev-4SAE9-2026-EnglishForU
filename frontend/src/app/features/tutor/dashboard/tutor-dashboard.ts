import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-tutor-dashboard',
  templateUrl: './tutor-dashboard.html'
})
export class TutorDashboardComponent implements OnInit {
  userName: string = '';
  
  // Mock Data - You can replace these with real API calls to your Courses Microservice later
  stats = [
    { label: 'My Students', value: '24', color: 'text-[#0066FF]', bg: 'bg-blue-50' },
    { label: 'Upcoming Classes', value: '3', color: 'text-green-600', bg: 'bg-green-50' },
    { label: 'Pending Quizzes', value: '12', color: 'text-orange-600', bg: 'bg-orange-50' }
  ];

  todaySchedule = [
    { time: '09:00 AM', course: 'Business English Pro', level: 'B2', platform: 'Zoom Meeting' },
    { time: '11:30 AM', course: 'IELTS Writing Workshop', level: 'C1', platform: 'Teams' },
    { time: '02:00 PM', course: 'General English Basics', level: 'A2', platform: 'Room 302' },
    { time: '04:30 PM', course: 'Conversation Club', level: 'B1', platform: 'Social Lounge' }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    this.userName = user ? user.name : 'Tutor';
  }
}