import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
<<<<<<< HEAD
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
=======
import { CommonModule } from '@angular/common'; // <--- AJOUTÉ
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { SharedModule } from './features/shared/shared.module';
<<<<<<< HEAD

=======
import { CoursesComponent } from './features/courses/courses';

// Vérifiez que ces chemins sont corrects
import { QuizComponent } from './features/quiz/quiz/quiz.component';
import { AddQuizComponent } from './features/quiz/add-quiz/add-quiz.component';
import { CourseDetailsComponent } from './features/courses/course-details/course-details.component';
import { QuizDetailsComponent } from './features/quiz/quiz-details/quiz-details.component';

// Importez les modules de fonctionnalités
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
import { UserModule } from './features/user-module';
import { AdminModule } from './features/admin/admin-module';
import { TutorModule } from './features/tutor/tutor-module';
import { StudentModule } from './features/student/student.module';
<<<<<<< HEAD
import { FormsModule } from '@angular/forms';

=======
import { AuthInterceptor } from './guards/auth.interceptor';
>>>>>>> 21f8a6f (metier avancer + controle de saisie)

@NgModule({
  declarations: [
    AppComponent,
<<<<<<< HEAD

  ],
  imports: [
    BrowserModule,
=======
    CoursesComponent,
    QuizComponent,
    AddQuizComponent,
    CourseDetailsComponent,
    QuizDetailsComponent
    // CreateQuizComponent <--- SUPPRIMÉ car il n'est pas importé et cause l'erreur TS2552
  ],
  imports: [
    BrowserModule,
    CommonModule, // <--- AJOUTÉ pour corriger ngClass, ngIf et les pipes
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
    AppRoutingModule,
    SharedModule,
    ReactiveFormsModule,
    RouterModule,
    HttpClientModule,
<<<<<<< HEAD
=======
    UserModule,
    AdminModule,
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
    TutorModule,
    StudentModule,
    FormsModule
  ],
<<<<<<< HEAD
  providers: [],
=======
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
  bootstrap: [AppComponent]
})
export class AppModule { }
