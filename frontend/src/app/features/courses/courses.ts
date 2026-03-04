import { Component } from '@angular/core';

@Component({
  selector: 'app-courses',
  templateUrl: './courses.html'
})
export class CoursesComponent {
  categories = ['All', 'General English', 'IELTS Prep', 'Business English', 'Kids & Teens'];
  selectedCategory = 'All';

  courses = [
    {
      title: 'General English Foundation',
      level: 'A1 - A2',
      duration: '8 Weeks',
      price: '199',
      image: '📚',
      desc: 'Master the basics of everyday communication and grammar.',
      category: 'General English'
    },
    {
      title: 'IELTS Academic Masterclass',
      level: 'B2 - C1',
      duration: '12 Weeks',
      price: '350',
      image: '🎓',
      desc: 'Intensive preparation for the IELTS exam with mock tests.',
      category: 'IELTS Prep'
    },
    {
      title: 'Business Communication',
      level: 'B1 - B2',
      duration: '6 Weeks',
      price: '250',
      image: '💼',
      desc: 'Focus on emails, meetings, and professional presentations.',
      category: 'Business English'
    },
    {
      title: 'Advanced Conversation Club',
      level: 'C1 - C2',
      duration: '4 Weeks',
      price: '150',
      image: '🗣️',
      desc: 'Perfect your fluency with native speakers in real scenarios.',
      category: 'General English'
    }
  ];
}