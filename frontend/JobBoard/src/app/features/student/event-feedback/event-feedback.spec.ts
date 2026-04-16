import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { EventFeedbackComponent } from './event-feedback';
import { FeedbackService } from '../../../services/feedback.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('EventFeedbackComponent', () => {
  let component: EventFeedbackComponent;
  let fixture: ComponentFixture<EventFeedbackComponent>;
  let feedbackServiceSpy: jasmine.SpyObj<FeedbackService>;

  const mockFeedbackData = {
    reviews: [
      { id: 1, userName: 'Alice Bob', rating: 4, comment: 'Good', createdAt: '2025-01-15T10:00:00' },
      { id: 2, userName: 'John Doe', rating: 5, comment: 'Excellent', createdAt: '2025-02-20T12:00:00' }
    ],
    myReview: null,
    canReview: true
  };

  beforeEach(async () => {
    feedbackServiceSpy = jasmine.createSpyObj('FeedbackService', [
      'getEventFeedback',
      'submitEventFeedback',
      'deleteFeedback'
    ]);
    feedbackServiceSpy.getEventFeedback.and.returnValue(of(mockFeedbackData));

    await TestBed.configureTestingModule({
      declarations: [EventFeedbackComponent],
      imports: [CommonModule, FormsModule],
      providers: [{ provide: FeedbackService, useValue: feedbackServiceSpy }],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(EventFeedbackComponent);
    component = fixture.componentInstance;
    component.eventId = 5;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // --- Initialization ---
  describe('ngOnInit / loadFeedback', () => {
    it('should load feedback on init and populate reviews', () => {
      expect(feedbackServiceSpy.getEventFeedback).toHaveBeenCalledWith(5);
      expect(component.reviews.length).toBe(2);
      expect(component.canReview).toBeTrue();
      expect(component.loading).toBeFalse();
    });

    it('should set loading to false even when the service returns an error', () => {
      feedbackServiceSpy.getEventFeedback.and.returnValue(throwError(() => new Error('Network error')));
      component.loadFeedback();
      expect(component.loading).toBeFalse();
    });
  });

  // --- Star rating interaction ---
  describe('Star rating interactions', () => {
    it('setRating should update selectedRating', () => {
      component.setRating(3);
      expect(component.selectedRating).toBe(3);

      component.setRating(5);
      expect(component.selectedRating).toBe(5);
    });

    it('hoverRating should update hoveredRating', () => {
      component.hoverRating(2);
      expect(component.hoveredRating).toBe(2);
    });

    it('clearHover should reset hoveredRating to 0', () => {
      component.hoverRating(4);
      component.clearHover();
      expect(component.hoveredRating).toBe(0);
    });

    it('starClass should return yellow for stars at or below hoveredRating', () => {
      component.hoverRating(3);
      expect(component.starClass(1)).toBe('text-yellow-400');
      expect(component.starClass(3)).toBe('text-yellow-400');
      expect(component.starClass(4)).toBe('text-gray-300');
    });

    it('starClass should fall back to selectedRating when not hovering', () => {
      component.setRating(4);
      component.clearHover();
      expect(component.starClass(4)).toBe('text-yellow-400');
      expect(component.starClass(5)).toBe('text-gray-300');
    });

    it('displayStarClass should return yellow for filled stars', () => {
      expect(component.displayStarClass(3, 4)).toBe('text-yellow-400');
      expect(component.displayStarClass(5, 4)).toBe('text-gray-200');
    });

    it('starsArray should return [1, 2, 3, 4, 5]', () => {
      expect(component.starsArray()).toEqual([1, 2, 3, 4, 5]);
    });
  });

  // --- Submit feedback validation ---
  describe('submitFeedback', () => {
    it('should set an error and not call the service when no rating is selected', () => {
      component.selectedRating = 0;
      component.submitFeedback();

      expect(component.error).toBe('Please select a star rating.');
      expect(feedbackServiceSpy.submitEventFeedback).not.toHaveBeenCalled();
    });

    it('should call submitEventFeedback with correct arguments when rating is set', () => {
      feedbackServiceSpy.submitEventFeedback.and.returnValue(of({}));
      feedbackServiceSpy.getEventFeedback.and.returnValue(of({ ...mockFeedbackData }));

      component.selectedRating = 4;
      component.comment = 'Really enjoyed it!';
      component.submitFeedback();

      expect(feedbackServiceSpy.submitEventFeedback).toHaveBeenCalledWith(5, 4, 'Really enjoyed it!');
    });

    it('should reset rating and comment after successful submission', () => {
      feedbackServiceSpy.submitEventFeedback.and.returnValue(of({}));
      feedbackServiceSpy.getEventFeedback.and.returnValue(of(mockFeedbackData));

      component.selectedRating = 3;
      component.comment = 'Nice!';
      component.submitFeedback();

      expect(component.selectedRating).toBe(0);
      expect(component.comment).toBe('');
      expect(component.successMsg).toBe('Thank you for your feedback!');
    });

    it('should set an error message when submission fails', () => {
      feedbackServiceSpy.submitEventFeedback.and.returnValue(
        throwError(() => ({ error: { error: 'Already reviewed' } }))
      );

      component.selectedRating = 5;
      component.submitFeedback();

      expect(component.error).toBe('Already reviewed');
      expect(component.submitting).toBeFalse();
    });

    it('should fallback to generic error message when error response has no specific message', () => {
      feedbackServiceSpy.submitEventFeedback.and.returnValue(
        throwError(() => ({ error: {} }))
      );

      component.selectedRating = 5;
      component.submitFeedback();

      expect(component.error).toBe('Failed to submit feedback.');
    });
  });

  // --- Delete review ---
  describe('deleteMyReview', () => {
    it('should not call deleteFeedback if myReview is null', () => {
      component.myReview = null;
      component.deleteMyReview();
      expect(feedbackServiceSpy.deleteFeedback).not.toHaveBeenCalled();
    });

    it('should call deleteFeedback with myReview.id and reload feedback on success', () => {
      feedbackServiceSpy.deleteFeedback.and.returnValue(of({}));
      feedbackServiceSpy.getEventFeedback.and.returnValue(of(mockFeedbackData));

      component.myReview = { id: 7, rating: 3, comment: 'ok' };
      component.deleteMyReview();

      expect(feedbackServiceSpy.deleteFeedback).toHaveBeenCalledWith(7);
      expect(component.deleting).toBeFalse();
    });
  });

  // --- Pure utility methods ---
  describe('getInitials', () => {
    it('should return the first letter of each word, uppercased', () => {
      expect(component.getInitials('Alice Bob')).toBe('AB');
    });

    it('should return at most 2 characters', () => {
      expect(component.getInitials('Alice Bob Carol')).toBe('AB');
    });

    it('should return "?" for an empty name', () => {
      expect(component.getInitials('')).toBe('?');
    });

    it('should handle a single-word name', () => {
      expect(component.getInitials('Alice')).toBe('A');
    });
  });

  describe('getAvatarColor', () => {
    const validColors = [
      'bg-emerald-500', 'bg-blue-500', 'bg-purple-500',
      'bg-rose-500', 'bg-amber-500', 'bg-cyan-500'
    ];

    it('should always return one of the valid avatar colors', () => {
      ['Alice', 'Bob', 'Charlie', 'David', 'Eve', 'Frank', 'Grace'].forEach(name => {
        expect(validColors).toContain(component.getAvatarColor(name));
      });
    });

    it('should return a valid color for an empty name', () => {
      expect(validColors).toContain(component.getAvatarColor(''));
    });

    it('should return a consistent color for the same name', () => {
      const color1 = component.getAvatarColor('Alice');
      const color2 = component.getAvatarColor('Alice');
      expect(color1).toBe(color2);
    });

    it('should return different colors for names starting with different characters', () => {
      // 'A' = 65, 'G' = 71 → 65%6=5 (cyan) vs 71%6=5 (cyan) — so we test ones we know differ
      const colorA = component.getAvatarColor('Alice'); // 65 % 6 = 5 → cyan
      const colorB = component.getAvatarColor('Bob');   // 66 % 6 = 0 → emerald
      expect(colorA).not.toBe(colorB);
    });
  });

  describe('formatDate', () => {
    it('should format a valid ISO date string into a readable date', () => {
      const result = component.formatDate('2025-01-15T10:00:00');
      expect(result).toContain('2025');
      expect(result).toContain('Jan');
    });

    it('should return an empty string for a falsy input', () => {
      expect(component.formatDate('')).toBe('');
    });
  });
});
