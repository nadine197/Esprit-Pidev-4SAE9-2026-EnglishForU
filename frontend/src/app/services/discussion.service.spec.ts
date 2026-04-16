import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DiscussionService } from './discussion.service';

describe('DiscussionService', () => {
  let service: DiscussionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DiscussionService]
    });
    service = TestBed.inject(DiscussionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch all groups', () => {
    const dummyGroups = [{ id: '1', groupName: 'Group A' }];

    service.getAllGroups().subscribe(groups => {
      expect(groups.length).toBe(1);
      expect(groups).toEqual(dummyGroups);
    });

    const req = httpMock.expectOne(`${service['apiUrl']}/groups/all`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyGroups);
  });
});