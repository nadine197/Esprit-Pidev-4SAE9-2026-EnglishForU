import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { StudyGroup } from '../../models/study-group';
import { StudyGroupService } from 'src/app/services/study-group.service';

function uuidValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (!val || typeof val !== 'string') return null;
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  return uuidRegex.test(val.trim()) ? null : { invalidUuid: true };
}

function uuidListValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (!val || typeof val !== 'string' || val.trim() === '') return null;
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  const ids = val.split(',').map((s: string) => s.trim()).filter((s: string) => s);
  return ids.every((id: string) => uuidRegex.test(id)) ? null : { invalidUuidList: true };
}

@Component({
  selector: 'app-study-group-form',
  templateUrl: './study-group-form.component.html',
  styleUrls: ['./study-group-form.component.css']
})
export class StudyGroupFormComponent implements OnInit {

  groupForm!: FormGroup;
  isEditMode = false;
  groupId!: number;
  submitError = '';

  constructor(
    private fb: FormBuilder,
    private service: StudyGroupService,
    private route: ActivatedRoute,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.groupForm = this.fb.group({
      name:        ['', Validators.required],
      level:       ['', Validators.required],
      location:    ['', Validators.required],
      maxCapacity: [1,  [Validators.required, Validators.min(1)]],
      startdate:   ['', Validators.required],
      enddate:     ['', Validators.required],
      status:      ['ACTIVE', Validators.required],
      courseId:    [null, Validators.required],
      tutorId:     ['', [Validators.required, uuidValidator]],
      studentsIds: ['', uuidListValidator]
    });

    const raw = this.route.snapshot.paramMap.get('id');

    // ✅ Garde contre NaN — seulement si id est un nombre valide
    if (raw && !isNaN(Number(raw))) {
      this.isEditMode = true;
      this.groupId    = Number(raw);
      this.loadGroup(this.groupId);
    }
  }

  loadGroup(id: number): void {
    this.service.getById(id).subscribe({
      next: (group) => {
        this.groupForm.patchValue({
          ...group,
          startdate:   group.startdate?.substring(0, 10) ?? '',
          enddate:     group.enddate?.substring(0, 10) ?? '',
          studentsIds: group.studentsIds?.join(', ') ?? ''
        });
      },
      error: () => this.submitError = 'Study group introuvable.'
    });
  }

  onSubmit(): void {
    if (this.groupForm.invalid) {
      this.groupForm.markAllAsTouched();
      return;
    }

    const formValue = this.groupForm.value;
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

    const studentsIds: string[] = formValue.studentsIds
      ? formValue.studentsIds
        .split(',')
        .map((s: string) => s.trim())
        .filter((s: string) => uuidRegex.test(s))
      : [];

    const group: StudyGroup = {
      groupId:     this.groupId,
      name:        formValue.name,
      level:       formValue.level,
      location:    formValue.location,
      maxCapacity: formValue.maxCapacity,
      startdate:   formValue.startdate,
      enddate:     formValue.enddate,
      status:      formValue.status,
      courseId:    formValue.courseId,
      tutorId:     formValue.tutorId.trim(),
      studentsIds
    };

    if (this.isEditMode) {
      this.service.update(this.groupId, group).subscribe({
        next:  () => this.router.navigate(['/study-groups']),
        error: (err) => this.submitError = err.error?.message ?? 'Erreur lors de la mise à jour.'
      });
    } else {
      this.service.create(group).subscribe({
        next:  () => this.router.navigate(['/study-groups']),
        error: (err) => this.submitError = err.error?.message ?? 'Erreur lors de la création.'
      });
    }
  }

  get f() { return this.groupForm.controls; }

  isInvalid(field: string): boolean {
    const c = this.groupForm.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
