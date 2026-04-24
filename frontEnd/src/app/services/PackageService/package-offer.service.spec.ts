import { TestBed } from '@angular/core/testing';

import { PackageOfferService } from './package-offer.service';

describe('PackageOfferService', () => {
  let service: PackageOfferService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PackageOfferService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
