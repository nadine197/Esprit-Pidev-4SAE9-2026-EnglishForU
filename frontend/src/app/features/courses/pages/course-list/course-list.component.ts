import { Component, OnInit } from '@angular/core';
import { CourseService } from 'src/app/services/course.service';
import { Course } from '../../models/courses';

@Component({
  selector: 'app-course-list',
  templateUrl: './course-list.component.html',
  styleUrls: ['./course-list.component.css']
})
export class CourseListComponent implements OnInit {

  courses: Course[] = [];

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses() {
    this.courseService.getAll().subscribe({
      next: (data) => {
        console.log("Courses from API:", data);
        this.courses = data;
      },
      error: (err) => {
        console.error("API ERROR", err);
      }
    });
  }

  delete(id: number) {
    this.courseService.delete(id).subscribe(() => {
      this.loadCourses();
    });
  }
  sortByDuration(){

    this.courseService.sortByDuration().subscribe({
      next: (data: Course[]) => {
        this.courses = data;
      },
      error: (err) => {
        console.error(err);
      }
    });

  }
  searchText = '';

  searchCourses(){

    this.courseService.search(this.searchText).subscribe({
      next: (data: Course[])=>{
        this.courses = data;
      },
      error: (err)=>{
        console.error(err);
      }
    })

  }
  downloadPDF(){

    this.courseService.downloadPdf().subscribe(blob => {

      const file = new Blob([blob],{type:'application/pdf'});

      const url = window.URL.createObjectURL(file);

      const link = document.createElement('a');

      link.href = url;
      link.download = "courses.pdf";

      link.click();

    });

  }
  downloadExcel(){

    this.courseService.downloadExcel().subscribe(blob => {

      const file = new Blob([blob],{
        type:'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      const url = window.URL.createObjectURL(file);

      const link = document.createElement('a');

      link.href = url;
      link.download = "courses.xlsx";

      link.click();

    });
  }


}
