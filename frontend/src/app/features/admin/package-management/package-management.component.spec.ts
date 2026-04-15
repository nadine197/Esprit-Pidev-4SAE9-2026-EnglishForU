import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PackageManagementComponent } from './package-management.component';

describe('PackageManagementComponent', () => {
  let component: PackageManagementComponent;
  let fixture: ComponentFixture<PackageManagementComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PackageManagementComponent]
    });
    fixture = TestBed.createComponent(PackageManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
