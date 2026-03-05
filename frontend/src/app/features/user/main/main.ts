import { Component, OnInit } from '@angular/core';
import { PackageOfferResponse } from 'src/app/models/package.models';
import { PackageOfferService } from 'src/app/services/PackageService/package-offer.service';

type UiPackageCard = {
  id: number;
  name: string;
  price: number;        // number
  popular: boolean;
  features: string[];
  durationLabel: string;
  description?: string;
  type?: string;
};

@Component({
  selector: 'app-main',
  templateUrl: './main.html'
})
export class MainComponent implements OnInit {

  isLoadingPackages = false;
  packagesError: string | null = null;

  // This is what your template uses
  packages: UiPackageCard[] = [];

  features = [
    { title: 'Expert-Led Courses', desc: 'Comprehensive English programs designed by certified instructors.', icon: 'book' },
    { title: 'Assessments & Quizzes', desc: 'Track your progress with regular evaluations and mock tests.', icon: 'check' },
    { title: 'Group Learning', desc: 'Collaborative learning groups matched by level for practice.', icon: 'users' },
    { title: 'Events & Workshops', desc: 'Engage in speaking clubs and exclusive workshops.', icon: 'calendar' }
  ];

  testimonials = [
    { name: 'Sarah Mitchell', role: 'IELTS Student', content: 'LinguaAcademy helped me achieve a band 8 in IELTS.' },
    { name: 'Ahmed Hassan', role: 'Business English', content: 'The program transformed my professional communication.' },
    { name: 'Maria Rodriguez', role: 'General English', content: 'Starting from scratch, I’m now confidently speaking.' }
  ];

  constructor(private packageOfferService: PackageOfferService) {}

  ngOnInit(): void {
    this.loadActivePackages();
  }
loadActivePackages(): void {
  this.isLoadingPackages = true;
  this.packagesError = null;

  this.packageOfferService.getActivePackages().subscribe({
    next: (res: PackageOfferResponse[]) => {
      const active = (res || []).filter(p => p.isActive); // ✅ only active

      const sorted = [...active].sort((a, b) => a.price - b.price);
      const popularId = sorted.length ? sorted[Math.floor(sorted.length / 2)].id : null;

      this.packages = sorted.map(p => this.toUiCard(p, p.id === popularId));
      this.isLoadingPackages = false;
    },
    error: (err) => {
      console.error(err);
      this.packagesError = 'Could not load packages. Please try again.';
      this.isLoadingPackages = false;
    }
  });
}

 private toUiCard(p: PackageOfferResponse, popular: boolean): UiPackageCard {
  const itemsCount = (p.items?.length ?? 0);

  // duration label: show months if divisible by ~30
  const months = Math.round(p.durationDays / 30);
  const durationLabel =
    p.durationDays >= 28 && p.durationDays <= 31
      ? '/month'
      : (p.durationDays % 30 === 0 && months > 1)
        ? `/${months} months`
        : `/${p.durationDays} days`;

  // features: limit for UI neatness
  const features = (p.features || []).filter(Boolean).slice(0, 6);

  return {
    id: p.id,
    name: this.titleCase(p.name),
    price: Number(p.price),
    popular,
    features: features.length ? features : [`${itemsCount} item(s) included`, `${p.durationDays} days access`],
    durationLabel,
    description: p.description,
    type: p.type
  };
}

private titleCase(s: string): string {
  return (s || '').replace(/\w\S*/g, t => t.charAt(0).toUpperCase() + t.slice(1).toLowerCase());
}
  onGetStarted(pkg: UiPackageCard): void {
    // hook this to your payment flow / route
    console.log('Selected package:', pkg);
    // e.g. this.router.navigate(['/checkout', pkg.id]);
  }
}