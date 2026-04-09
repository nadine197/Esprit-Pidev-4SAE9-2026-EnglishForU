import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, AbstractControl } from '@angular/forms';

@Component({
  selector: 'app-create-quiz',
  templateUrl: './create-quiz.component.html'
})
export class CreateQuizComponent implements OnInit {

  quizForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.quizForm = this.fb.group({
      titre:       ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      categorie:   ['', Validators.required],
      duree:       [null, [Validators.required, Validators.min(1), Validators.max(180)]],
      questions:   this.fb.array([this.creerQuestion()])
    });
  }

  // Getters pour accéder facilement aux champs
  get titre()       { return this.quizForm.get('titre')!; }
  get description() { return this.quizForm.get('description')!; }
  get categorie()   { return this.quizForm.get('categorie')!; }
  get duree()       { return this.quizForm.get('duree')!; }
  get questions()   { return this.quizForm.get('questions') as FormArray; }

  creerQuestion(): FormGroup {
    return this.fb.group({
      enonce:         ['', [Validators.required, Validators.minLength(5)]],
      bonneReponse:   ['', Validators.required],
      choixA:         ['', Validators.required],
      choixB:         ['', Validators.required],
      choixC:         ['', Validators.required],
    });
  }

  ajouterQuestion(): void {
    if (this.questions.length < 20) {
      this.questions.push(this.creerQuestion());
    }
  }

  supprimerQuestion(index: number): void {
    if (this.questions.length > 1) {
      this.questions.removeAt(index);
    }
  }

  getQuestion(index: number): FormGroup {
    return this.questions.at(index) as FormGroup;
  }

  // Retourne le message d'erreur selon le champ
  getErreur(control: AbstractControl | null): string {
    if (!control || !control.errors || !control.touched) return '';
    if (control.errors['required'])   return 'Ce champ est obligatoire.';
    if (control.errors['minlength'])  return `Minimum ${control.errors['minlength'].requiredLength} caractères.`;
    if (control.errors['maxlength'])  return `Maximum ${control.errors['maxlength'].requiredLength} caractères.`;
    if (control.errors['min'])        return `La valeur minimale est ${control.errors['min'].min}.`;
    if (control.errors['max'])        return `La valeur maximale est ${control.errors['max'].max}.`;
    return 'Valeur invalide.';
  }

  onSubmit(): void {
    if (this.quizForm.invalid) {
      this.quizForm.markAllAsTouched(); // Affiche toutes les erreurs
      return;
    }
    console.log('Quiz soumis :', this.quizForm.value);
    // Appel service ici : this.quizService.creerQuiz(this.quizForm.value).subscribe(...)
  }
}
