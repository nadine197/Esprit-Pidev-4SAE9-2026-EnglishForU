import { Component, OnInit } from '@angular/core';
import { PaymentService } from 'src/app/services/PackageService/payment.service';
import { PackageOfferService } from 'src/app/services/PackageService/package-offer.service';
import { PaymentResponse, PaymentStatus } from 'src/app/models/payment.models';
import { PackageOfferResponse } from 'src/app/models/package.models';

type ProviderFilter = '' | 'CASH' | 'STRIPE' | 'FLOUCI';
type TargetFilter = '' | number; // packageId

@Component({
  selector: 'app-payment-management',
  templateUrl: './payment-management.component.html',
  styleUrls: ['./payment-management.component.css']
})
export class PaymentManagementComponent implements OnInit {
  // raw from backend
  payments: PaymentResponse[] = [];

  // packages (for package name + filter)
  packages: PackageOfferResponse[] = [];
  packagesMap = new Map<number, string>(); // id -> name

  // filtered + paginated
  filteredPayments: PaymentResponse[] = [];
  pagedPayments: PaymentResponse[] = [];

  loading = false;
  loadingPackages = false;

  // filters (client-side)
  filterStatus: '' | PaymentStatus = '';
  filterProvider: ProviderFilter = '';
  filterPackageId: TargetFilter = ''; // ✅ new
  searchText = ''; // ✅ new

  // pagination
  page = 1;
  pageSize = 8;
  totalPages = 1;

  // update status modal
  showStatusModal = false;
  selectedPayment: PaymentResponse | null = null;
  newStatus: PaymentStatus = 'PENDING';

  constructor(
    private paymentService: PaymentService,
    private packageService: PackageOfferService
  ) {}

  ngOnInit(): void {
    // load both in parallel
    this.loadPackages();
    this.loadPayments();
  }

  loadPackages() {
    this.loadingPackages = true;

    this.packageService.getAllPackages().subscribe({
      next: (list) => {
        this.packages = list || [];
        this.packagesMap = new Map(this.packages.map(p => [p.id, p.name]));
        this.loadingPackages = false;

        // packages affect display/filter => re-apply filters
        this.applyFilters();
      },
      error: () => (this.loadingPackages = false)
    });
  }

  loadPayments() {
    this.loading = true;

    this.paymentService.getAllPayments().subscribe({
      next: (data) => {
        this.payments = (data || []).slice().sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
        this.loading = false;
        this.applyFilters();
      },
      error: () => (this.loading = false)
    });
  }



  // ✅ filtering client-side
  applyFilters() {
    const status = this.filterStatus;
    const provider = this.filterProvider;
    const pkgId = this.filterPackageId;
    const q = (this.searchText || '').trim().toLowerCase();

    this.filteredPayments = this.payments.filter(p => {
      const okStatus = !status || p.status === status;

      // provider = paymentMethod (and provider if you keep it)
      const okProvider =
        !provider ||
        p.paymentMethod === provider ||
        (p.provider ? p.provider === provider : false);


      // search student name (works once backend sends studentFullName)
      const fullName = (p.studentFullName || '').toLowerCase();
      const okSearch = !q || fullName.includes(q) || String(p.studentId).includes(q);

      return okStatus && okProvider  && okSearch;
    });

    this.page = 1;
    this.recomputePagination();
  }

  resetFilters() {
    this.filterStatus = '';
    this.filterProvider = '';
    this.filterPackageId = '';
    this.searchText = '';
    this.applyFilters();
  }

  // ✅ pagination
  recomputePagination() {
    this.totalPages = Math.max(1, Math.ceil(this.filteredPayments.length / this.pageSize));
    if (this.page > this.totalPages) this.page = this.totalPages;
    if (this.page < 1) this.page = 1;

    const start = (this.page - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.pagedPayments = this.filteredPayments.slice(start, end);
  }

  nextPage() {
    if (this.page >= this.totalPages) return;
    this.page++;
    this.recomputePagination();
  }

  prevPage() {
    if (this.page <= 1) return;
    this.page--;
    this.recomputePagination();
  }

  goToPage(n: number) {
    this.page = n;
    this.recomputePagination();
  }

  setPageSize(size: number) {
    this.pageSize = size;
    this.page = 1;
    this.recomputePagination();
  }

  // modal
  openStatusModal(p: PaymentResponse) {
    this.selectedPayment = p;
    this.newStatus = p.status;
    this.showStatusModal = true;
  }

  saveStatus() {
    if (!this.selectedPayment) return;

    this.paymentService.updatePaymentStatus(this.selectedPayment.id, this.newStatus).subscribe({
      next: (updated) => {
        const idx = this.payments.findIndex(x => x.id === updated.id);
        if (idx !== -1) this.payments[idx] = updated;

        this.payments = this.payments.slice().sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));

        this.showStatusModal = false;
        this.selectedPayment = null;
        this.applyFilters();
      }
    });
  }

  deletePayment(p: PaymentResponse) {
    if (p.status !== 'PENDING') {
      alert('Only PENDING payments can be deleted.');
      return;
    }
    if (!confirm(`Delete payment #${p.id}?`)) return;

    this.paymentService.deletePayment(p.id).subscribe({
      next: () => {
        this.payments = this.payments.filter(x => x.id !== p.id);
        this.applyFilters();
      }
    });
  }

  // UI helpers
  get showingFrom(): number {
    if (!this.filteredPayments.length) return 0;
    return (this.page - 1) * this.pageSize + 1;
  }
  get showingTo(): number {
    return Math.min(this.page * this.pageSize, this.filteredPayments.length);
  }

  get pages(): number[] {
    const arr: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) arr.push(i);
    return arr;
  }
}