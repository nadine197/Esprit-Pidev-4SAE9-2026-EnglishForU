import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PackageOfferResponse, AddPackageItemRequest } from 'src/app/models/package.models';
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

  // ✅ Add Item modal state
  showItemModal = false;
  selectedPackageForItems: PackageOfferResponse | null = null;

  packageForm: FormGroup;

  // ✅ Add Item form
  itemForm: FormGroup;

  constructor(private packageService: PackageOfferService, private fb: FormBuilder) {
    this.packageForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      type: ['COURSE_ONLY', Validators.required],
      durationDays: [1, [Validators.required, Validators.min(1)]],
      price: [0, [Validators.required, Validators.min(0)]],
      isActive: [true, Validators.required]
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
      isActive: true
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
      isActive: !!p.isActive
    });

    this.showModal = true;
  }

  savePackage() {
    if (this.packageForm.invalid) return;

    const payload = this.packageForm.value;

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

  // ✅ OPEN Add Item modal
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

  // ✅ SAVE item
  saveItem() {
    if (this.itemForm.invalid || !this.selectedPackageForItems) return;

    const payload: AddPackageItemRequest = this.itemForm.value;

    this.packageService.addItem(this.selectedPackageForItems.id, payload).subscribe({
      next: (updatedPackage) => {
        // update list instantly without reload
        const idx = this.packages.findIndex(x => x.id === updatedPackage.id);
        if (idx !== -1) this.packages[idx] = updatedPackage;

        this.closeItemModal();
      }
    });
  }
}