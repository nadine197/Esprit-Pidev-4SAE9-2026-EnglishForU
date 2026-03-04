import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PromoService } from 'src/app/services/PackageService/promo.service';

@Component({
  selector: 'app-promo-management',
  templateUrl: './promo-management.component.html',
  styleUrls: ['./promo-management.component.css']
})
export class PromoManagementComponent implements OnInit {
  promos: any[] = [];
  loading = false;

  showModal = false;
  isEditMode = false;
  selectedId: number | null = null;

  promoForm: FormGroup;

  constructor(private promoService: PromoService, private fb: FormBuilder) {
    this.promoForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(3)]],
      discountType: ['PERCENTAGE', Validators.required],
      discountValue: [0, [Validators.required, Validators.min(1)]],
      startDate: [''],
      endDate: [''],
      usageLimit: [0, [Validators.min(0)]],
      active: [true, Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadPromos();
  }

  loadPromos() {
    this.loading = true;
    this.promoService.getAllPromos().subscribe({
      next: (data) => {
        this.promos = data;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  openAddModal() {
    this.isEditMode = false;
    this.selectedId = null;
    this.promoForm.reset({
      code: '',
      discountType: 'PERCENTAGE',
      discountValue: 0,
      startDate: '',
      endDate: '',
      usageLimit: 0,
      active: true
    });
    this.showModal = true;
  }

  openEditModal(p: any) {
    this.isEditMode = true;
    this.selectedId = p.id;

    this.promoForm.patchValue({
      code: p.code,
      discountType: p.discountType,
      discountValue: p.discountValue,
      startDate: p.startDate || '',
      endDate: p.endDate || '',
      usageLimit: p.usageLimit || 0,
      active: !!p.active
    });

    this.showModal = true;
  }

  savePromo() {
    if (this.promoForm.invalid) return;

    const payload = this.promoForm.value;

    if (this.isEditMode && this.selectedId) {
      this.promoService.updatePromo(this.selectedId, payload).subscribe({
        next: () => {
          this.loadPromos();
          this.showModal = false;
        }
      });
    } else {
      this.promoService.createPromo(payload).subscribe({
        next: () => {
          this.loadPromos();
          this.showModal = false;
        }
      });
    }
  }

  toggleActive(p: any) {
    const call = p.active ? this.promoService.disablePromo(p.id) : this.promoService.enablePromo(p.id);
    call.subscribe({
      next: () => {
        p.active = !p.active; // immediate UI update
      }
    });
  }

  deletePromo(id: number) {
    if (!confirm('Delete this promo code?')) return;
    this.promoService.deletePromo(id).subscribe({
      next: () => this.loadPromos()
    });
  }
}