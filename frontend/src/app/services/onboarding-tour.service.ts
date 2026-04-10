import { Injectable } from '@angular/core';
import introJs from 'intro.js';

interface TourStepConfig {
  element: string;
  title: string;
  intro: string;
  position?: 'top' | 'right' | 'bottom' | 'left';
}

@Injectable({
  providedIn: 'root'
})
export class OnboardingTourService {
  private readonly storagePrefix = 'efu:onboarding:tour:';
  private readonly startedToursInSession = new Set<string>();
  private activeTourId: string | null = null;

  startProductTour(force = false): void {
    this.startTour(
      'product-nav:v2',
      [
        {
          element: '#tour-courses-link',
          title: 'Courses',
          intro: 'Start here to browse classes and jump into the level that matches your current learning goals.',
          position: 'bottom'
        },
        {
          element: '#tour-threads-link',
          title: 'Threads',
          intro: 'Open Threads to ask questions, post class notes, and collaborate with learners in real time.',
          position: 'bottom'
        },
        {
          element: '#tour-report-issue',
          title: 'Report Issue',
          intro: 'Use this quick action whenever you find a bug or blocked flow so Help Desk can act fast.',
          position: 'left'
        }
      ],
      force
    );
  }

  startReportModalTour(force = false): void {
    this.startTour(
      'report-modal:v1',
      [
        {
          element: '#tour-report-modal',
          title: 'Smart Incident Report',
          intro: 'A clear report helps Help Desk reproduce the issue quickly and reduce back-and-forth.',
          position: 'left'
        },
        {
          element: '#report-title',
          title: 'Title',
          intro: 'Summarize the problem in one line, like “Cannot upload PDF in discussion thread”.',
          position: 'bottom'
        },
        {
          element: '#report-category',
          title: 'Category',
          intro: 'Choose BUG, ISSUE, or FEATURE_REQUEST so triage rules route your ticket correctly.',
          position: 'bottom'
        },
        {
          element: '#report-severity',
          title: 'Severity',
          intro: 'Set impact level honestly. Critical is for major blockers affecting core user workflows.',
          position: 'bottom'
        },
        {
          element: '#report-description',
          title: 'Description',
          intro: 'Describe what happened, what you expected, and the impact on your work.',
          position: 'top'
        },
        {
          element: '#report-steps',
          title: 'Steps To Reproduce',
          intro: 'List exact steps to reproduce. This is the most valuable field for fast fixes.',
          position: 'top'
        },
        {
          element: '#tour-report-submit',
          title: 'Submit',
          intro: 'Submit when ready. The platform already attaches page URL, user agent, and version metadata.',
          position: 'top'
        }
      ],
      force
    );
  }

  startThreadPageTour(force = false): void {
    this.startTour(
      'threads-page:v1',
      [
        {
          element: '#tour-thread-filters',
          title: 'Find Relevant Conversations',
          intro: 'Filter by scope and CEFR level to focus on posts that match your current context.',
          position: 'bottom'
        },
        {
          element: '#tour-thread-composer',
          title: 'Start A New Thread',
          intro: 'Create a post, share a lesson takeaway, or ask for help from tutors and classmates.',
          position: 'right'
        },
        {
          element: '#tour-thread-post-types',
          title: 'Rich Post Types',
          intro: 'Choose Text, Media (image/PDF), or Quiz to make your post engaging and actionable.',
          position: 'right'
        },
        {
          element: '#tour-thread-publish',
          title: 'Publish',
          intro: 'Publish and check reactions/comments to keep the discussion moving.',
          position: 'top'
        },
        {
          element: '#tour-thread-first-post',
          title: 'Join Existing Threads',
          intro: 'Open thread details, react, and comment to support peers or ask follow-up questions.',
          position: 'left'
        }
      ],
      force
    );
  }

  startHelpdeskBoardTour(force = false): void {
    this.startTour(
      'helpdesk-board:v1',
      [
        {
          element: '#tour-helpdesk-search',
          title: 'Search Queue',
          intro: 'Quickly locate tickets by keywords before triage or follow-up.',
          position: 'bottom'
        },
        {
          element: '#tour-helpdesk-severity-filter',
          title: 'Severity Focus',
          intro: 'Filter by severity to prioritize high-impact incidents first.',
          position: 'bottom'
        },
        {
          element: '#tour-helpdesk-board',
          title: 'Kanban Workflow',
          intro: 'Drag tickets across statuses to reflect lifecycle: NEW → TRIAGED → IN_PROGRESS → DONE/CLOSED.',
          position: 'top'
        },
        {
          element: '.ticket-card',
          title: 'Ticket Detail Actions',
          intro: 'Open any card to update fields, request more info, and post timeline comments with context.',
          position: 'right'
        },
        {
          element: '#tour-helpdesk-join-threads',
          title: 'Collaborate In Threads',
          intro: 'Jump to community threads when you need extra details from learners or tutors.',
          position: 'left'
        }
      ],
      force
    );
  }

  startHelpdeskTicketModalTour(force = false): void {
    this.startTour(
      'helpdesk-ticket-modal:v1',
      [
        {
          element: '#tour-helpdesk-ticket-modal',
          title: 'Ticket Deep Dive',
          intro: 'Use this workspace to triage, investigate, and document resolution progress.',
          position: 'left'
        },
        {
          element: '#tour-helpdesk-ticket-status',
          title: 'Status Control',
          intro: 'Move tickets through the lifecycle as work progresses and evidence is collected.',
          position: 'left'
        },
        {
          element: '#tour-helpdesk-request-info',
          title: 'Request More Info',
          intro: 'Ask clear follow-up questions when reproduction context is incomplete.',
          position: 'left'
        },
        {
          element: '#tour-helpdesk-comment',
          title: 'Timeline Comments',
          intro: 'Post visible updates so teammates and reporters can track troubleshooting steps.',
          position: 'left'
        },
        {
          element: '#tour-helpdesk-save-ticket',
          title: 'Save Changes',
          intro: 'Save after edits to status, severity, and technical notes to keep records accurate.',
          position: 'top'
        }
      ],
      force
    );
  }

  resetTour(): void {
    const knownTourIds = [
      'product-nav:v2',
      'report-modal:v1',
      'threads-page:v1',
      'helpdesk-board:v1',
      'helpdesk-ticket-modal:v1'
    ];

    knownTourIds.forEach((tourId) => {
      try {
        localStorage.removeItem(this.getStorageKey(tourId));
      } catch {
        // Ignore localStorage access issues.
      }
    });

    this.startedToursInSession.clear();
    this.activeTourId = null;
  }

  private startTour(
    tourId: string,
    configuredSteps: TourStepConfig[],
    force: boolean,
    retryCount = 0
  ): void {
    if (!force && (this.startedToursInSession.has(tourId) || this.hasCompletedTour(tourId))) {
      return;
    }

    if (this.activeTourId && this.activeTourId !== tourId) {
      if (retryCount < 6) {
        setTimeout(() => this.startTour(tourId, configuredSteps, force, retryCount + 1), 700);
      }
      return;
    }

    const availableSteps = configuredSteps.filter((step) => !!document.querySelector(step.element));
    if (availableSteps.length === 0) {
      if (retryCount < 6) {
        setTimeout(() => this.startTour(tourId, configuredSteps, force, retryCount + 1), 350);
      }
      return;
    }

    const intro = introJs();
    this.activeTourId = tourId;

    intro.setOptions({
      steps: availableSteps,
      showProgress: true,
      showBullets: false,
      exitOnEsc: true,
      exitOnOverlayClick: false,
      disableInteraction: false,
      nextLabel: 'Next',
      prevLabel: 'Back',
      doneLabel: 'Finish',
      skipLabel: 'Skip',
      tooltipClass: 'efu-onboarding-tooltip'
    });

    const finalizeTour = () => {
      this.markTourCompleted(tourId);
      this.startedToursInSession.add(tourId);

      if (this.activeTourId === tourId) {
        this.activeTourId = null;
      }
    };

    intro.oncomplete(finalizeTour);
    intro.onexit(finalizeTour);
    intro.start();
  }

  private hasCompletedTour(tourId: string): boolean {
    try {
      return localStorage.getItem(this.getStorageKey(tourId)) === 'done';
    } catch {
      return false;
    }
  }

  private markTourCompleted(tourId: string): void {
    try {
      localStorage.setItem(this.getStorageKey(tourId), 'done');
    } catch {
      // Ignore localStorage access issues.
    }
  }

  private getStorageKey(tourId: string): string {
    return `${this.storagePrefix}${tourId}`;
  }
}
