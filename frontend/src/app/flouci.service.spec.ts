import { TestBed } from '@angular/core/testing';

import { FlouciService } from './flouci.service';

describe('FlouciService', () => {
  let service: FlouciService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FlouciService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
