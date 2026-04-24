import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PackageOfferResponse, AddPackageItemRequest, CreatePackageOfferRequest } from 'src/app/models/package.models';
import { PackageOfferService } from 'src/app/services/PackageService/package-offer.service';

@Component({
  selector: 'app-package-management',
  templateUrl: './package-management.component.html',
  styleUrls: ['./package-management.component.css']
})
export class PackageManagementComponent implements OnInit {
  packages: PackageOfferResponse[] = [];
  loading = false;

  showModal = false;
  isEditMode = false;
  selectedId: number | null = null;

  showItemModal = false;
  selectedPackageForItems: PackageOfferResponse | null = null;

  packageForm: FormGroup;
  itemForm: FormGroup;

  constructor(private packageService: PackageOfferService, private fb: FormBuilder) {
    this.packageForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      type: ['COURSE_ONLY', Validators.required],
      durationDays: [1, [Validators.required, Validators.min(1)]],
      price: [0, [Validators.required, Validators.min(0)]],
      isActive: [true, Validators.required],

      // ✅ NEW: textarea version (one feature per line)
      featuresText: ['']
    });

    this.itemForm = this.fb.group({
      itemType: ['', Validators.required],
      itemId: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadPackages();
  }

  loadPackages() {
    this.loading = true;
    this.packageService.getAllPackages().subscribe({
      next: (data) => {
        this.packages = data;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  openAddModal() {
    this.isEditMode = false;
    this.selectedId = null;

    this.packageForm.reset({
      name: '',
      description: '',
      type: 'COURSE_ONLY',
      durationDays: 1,
      price: 0,
      isActive: true,
      featuresText: '' // ✅ NEW
    });

    this.showModal = true;
  }

  openEditModal(p: PackageOfferResponse) {
    this.isEditMode = true;
    this.selectedId = p.id;

    this.packageForm.patchValue({
      name: p.name,
      description: p.description || '',
      type: p.type,
      durationDays: p.durationDays,
      price: p.price,
      isActive: !!p.isActive,

      // ✅ NEW: convert array -> textarea (line per feature)
      featuresText: (p.features || []).join('\n')
    });

    this.showModal = true;
  }

  private parseFeatures(text: string): string[] {
    return (text || '')
      .split('\n')
      .map(x => x.trim())
      .filter(Boolean);
  }

  savePackage() {
    if (this.packageForm.invalid) return;

    const v = this.packageForm.value;

    // ✅ Convert textarea -> string[]
    const payload: CreatePackageOfferRequest = {
      name: v.name,
      description: v.description,
      type: v.type,
      durationDays: Number(v.durationDays),
      price: Number(v.price),
      isActive: !!v.isActive,
      features: this.parseFeatures(v.featuresText) // ✅ NEW
    };

    if (this.isEditMode && this.selectedId) {
      this.packageService.updatePackage(this.selectedId, payload).subscribe({
        next: () => {
          this.loadPackages();
          this.showModal = false;
        }
      });
    } else {
      this.packageService.createPackage(payload).subscribe({
        next: () => {
          this.loadPackages();
          this.showModal = false;
        }
      });
    }
  }

  toggleActive(p: PackageOfferResponse) {
    const call = p.isActive
      ? this.packageService.disablePackage(p.id)
      : this.packageService.enablePackage(p.id);

    call.subscribe({
      next: () => {
        p.isActive = !p.isActive;
      }
    });
  }

  openAddItemModal(p: PackageOfferResponse) {
    this.selectedPackageForItems = p;
    this.itemForm.reset({
      itemType: '',
      itemId: null
    });
    this.showItemModal = true;
  }

  closeItemModal() {
    this.showItemModal = false;
    this.selectedPackageForItems = null;
  }

  saveItem() {
    if (this.itemForm.invalid || !this.selectedPackageForItems) return;

    const payload: AddPackageItemRequest = this.itemForm.value;

    this.packageService.addItem(this.selectedPackageForItems.id, payload).subscribe({
      next: (updatedPackage) => {
        const idx = this.packages.findIndex(x => x.id === updatedPackage.id);
        if (idx !== -1) this.packages[idx] = updatedPackage;

        this.closeItemModal();
      }
    });
  }
}