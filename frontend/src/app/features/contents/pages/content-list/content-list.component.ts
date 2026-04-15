import { Component, OnInit } from '@angular/core';
import { ContentService } from 'src/app/services/content.service';
import { Content } from '../../models/content';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-content-list',
  templateUrl: './content-list.component.html',
  styleUrls: ['./content-list.component.css']
})
export class ContentListComponent implements OnInit {

  contents: Content[] = [];
  loading = true;
  error = '';

  searchText = '';

  showChart = false;
  chart: any;

  constructor(private contentService: ContentService) {}

  ngOnInit(): void {
    this.loadContents();   // charger seulement les contenus
  }

  loadContents() {
    this.loading = true;

    this.contentService.getAll().subscribe({
      next: (data) => {
        this.contents = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to load contents';
        this.loading = false;
      }
    });
  }

  delete(id?: number) {

    if (!id) return;

    if (confirm('Are you sure you want to delete this content?')) {

      this.contentService.delete(id).subscribe({
        next: () => this.loadContents(),
        error: (err) => console.error(err)
      });

    }
  }

  searchContents(){

    this.contentService.search(this.searchText).subscribe({
      next: (data: Content[])=>{
        this.contents = data;
      },
      error: (err)=>{
        console.error(err);
      }
    });

  }

  downloadPDF(){

    this.contentService.downloadPdf().subscribe(blob => {

      const file = new Blob([blob],{type:'application/pdf'});

      const url = window.URL.createObjectURL(file);

      const link = document.createElement('a');

      link.href = url;
      link.download = "contents.pdf";

      link.click();

    });

  }

  loadStats(){

    this.showChart = true;

    this.contentService.getStatsByType().subscribe(data => {

      setTimeout(()=>{

        const labels = data.map((d:any)=> d[0]);
        const values = data.map((d:any)=> d[1]);

        if(this.chart){
          this.chart.destroy();
        }

        this.chart = new Chart("contentChart",{
          type:'bar',
          data:{
            labels: labels,
            datasets:[{
              label:'Contents by type',
              data: values,
              backgroundColor:[
                '#4CAF50',
                '#2196F3',
                '#FFC107'
              ]
            }]
          }
        });

      },100);

    });

  }
  history:string[] = [];
  showHistory = false;

  downloadHistory(){

    this.contentService.downloadHistory().subscribe(blob => {

      const file = new Blob([blob],{type:'text/plain'});

      const url = window.URL.createObjectURL(file);

      const link = document.createElement('a');

      link.href = url;
      link.download = "content-history.txt";

      link.click();

    });

  }
}
