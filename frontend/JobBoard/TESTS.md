# Frontend Unit Tests — Club, Event & Rating

## Testing Stack

| Tool | Role |
|------|------|
| **Jasmine** | The language used to write tests (`describe`, `it`, `expect`) |
| **Karma** | The runner that opens Chrome and executes the tests |

They always work together in Angular. Jasmine writes, Karma runs.

No Docker or backend needed — all HTTP calls are intercepted in memory by `HttpClientTestingModule`.

---

## How to Run

```bash
cd frontEnd/JobBoard
ng test --watch=false --browsers=ChromeHeadless
```

---

## Test Files Added

### 1. `src/app/services/auth.service.spec.ts` — 12 tests

Tests the authentication logic (token storage, login/logout).

| Test | What it checks |
|------|----------------|
| login stores token in localStorage | After login, `localStorage.getItem('token')` is set |
| login stores user in localStorage | After login, `localStorage.getItem('user')` is set |
| login with no token does not store anything | If server returns no token, nothing is saved |
| login sends credentials to correct endpoint | POST goes to `/api/auth/login` with correct body |
| getUser returns parsed user from localStorage | Returns the user object |
| getUser returns null when nothing stored | Returns `null` when localStorage is empty |
| logout removes token from localStorage | Token is gone after logout |
| logout removes user from localStorage | User is gone after logout |
| logout does not throw when localStorage is empty | No crash if called twice |
| isLoggedIn returns true when token exists | `true` when token is present |
| isLoggedIn returns false when no token | `false` when nothing is stored |
| isLoggedIn returns false after logout | `false` immediately after calling logout |
| signup POSTs to register-client endpoint | POST goes to `/api/auth/register-client` |

---

### 2. `src/app/services/event.service.spec.ts` — 22 tests

Tests all event HTTP calls and business logic (URL building, params).

| Test | What it checks |
|------|----------------|
| should be created | Service initializes |
| attaches Bearer token to every request | `Authorization: Bearer <token>` header is present |
| appends user email as userId param | `?userId=john@test.com` in URL |
| uses empty userId when no user stored | `?userId=` when localStorage is empty |
| builds userName from name + lastName | `?userName=John%20Doe` in participate URL |
| falls back to email when name is missing | `?userName=john%40test.com` when no name |
| uses only first name when lastName missing | `?userName=Alice` |
| getAllEvents → GET base URL | Correct method and endpoint |
| getEventsByClub → GET /club/5 | Correct club-specific URL |
| getEventById → GET /3 | Correct event URL |
| createEvent → POST with event body | Correct method, endpoint and body |
| updateEvent → PUT with update body | Correct method, endpoint and body |
| deleteEvent → DELETE /3 | Correct method and endpoint |
| participate → POST to /10/participate | Correct endpoint |
| acceptParticipant → PUT to /accept | Correct endpoint |
| rejectParticipant → PUT to /reject | Correct endpoint |
| getParticipants → GET participant list | Correct endpoint |
| getPendingParticipants → GET pending list | Correct endpoint |
| createPaymentIntent → POST with userEmail | Correct body |
| confirmPayment → POST full body | Correct body and endpoint |
| getMyPurchases → GET purchases endpoint | Correct endpoint |
| downloadPass → view=false in URL | Correct query param |
| openPass → view=true in URL | Correct query param |

---

### 3. `src/app/services/club.service.spec.ts` — 14 tests

Tests all club HTTP calls and membership management.

| Test | What it checks |
|------|----------------|
| should be created | Service initializes |
| attaches Bearer token to every request | `Authorization: Bearer <token>` header |
| appends user email as userId param | `?userId=alice@test.com` in URL |
| uses empty userId when no user stored | `?userId=` when localStorage is empty |
| getAllClubs → GET base URL | Correct method and endpoint |
| getClubById → GET /7 | Correct club URL |
| getMyClubs → GET /my | Correct endpoint |
| createClub → POST with userId and userName | Correct body, userId and userName in URL |
| updateClub → PUT to /7 | Correct method and endpoint |
| deleteClub → DELETE /7 | Correct method and endpoint |
| joinClub → POST with userId and userName | Correct endpoint and params |
| acceptMember → PUT to /accept | Correct endpoint |
| rejectMember → PUT to /reject | Correct endpoint |
| getMembers → GET full members list | Correct endpoint |
| getPendingMembers → GET only pending | Correct endpoint |

---

### 4. `src/app/services/feedback.service.spec.ts` — 10 tests

Tests the rating/feedback system for both events and clubs.

| Test | What it checks |
|------|----------------|
| should be created | Service initializes |
| getEventFeedback → GET with userId | Correct endpoint and userId param |
| getClubFeedback → GET with userId | Correct endpoint and userId param |
| getEventStats → GET stats endpoint | Returns average rating and total reviews |
| getClubStats → GET stats endpoint | Returns average rating and total reviews |
| submitEventFeedback → POST with targetType EVENT | Body has `targetType: 'EVENT'`, correct rating and comment |
| submitEventFeedback encodes userName in URL | `?userName=Alice%20Dev` is URL-encoded |
| submitEventFeedback accepts rating of 1 (minimum) | `rating: 1` in body |
| submitClubFeedback → POST with targetType CLUB | Body has `targetType: 'CLUB'` |
| deleteFeedback → DELETE /7 with userId | Correct endpoint and userId param |

---

### 5. `src/app/guards/auth.guard.spec.ts` — 6 tests

Tests the role-based access control guard.

| Test | What it checks |
|------|----------------|
| redirects to /login when not logged in | Returns `false`, navigates to `/login` |
| returns true when logged in, no role restriction | Access granted freely |
| returns true when user role matches required roles | STUDENT accessing STUDENT route |
| redirects to /main when role does not match | STUDENT trying ADMIN route |
| returns true for TUTOR accessing TUTOR-only route | Correct role access |
| redirects to /main when user has no role | Edge case — user without role field |

---

### 6. `src/app/features/student/event-form/event-form.spec.ts` — 26 tests

Tests the event creation/edit form — date validation, price rules, field errors.

| Test | What it checks |
|------|----------------|
| should create | Component initializes |
| starts in create mode (isEditMode = false) | Default state is create |
| has all required form controls | title, description, eventDate, location, paid, price, maxParticipants |
| starts with invalid form | Required fields are empty by default |
| date is valid when in the future | Future date passes `futureDateValidator` |
| date returns pastDate error when in the past | Past date fails validator |
| empty date has no pastDate error | `required` handles empty, not `pastDate` |
| paid=true adds required validator to price | Price becomes required |
| paid=true enforces minimum price of 0.5 | Price below 0.5 TND is rejected |
| paid=true enforces maximum price of 10000 | Price above 10,000 TND is rejected |
| paid=true accepts valid price between 0.5 and 10000 | Price of 50 TND is valid |
| paid=false clears price validators and resets value | Price is null and valid again |
| isPaid returns false by default | Getter works correctly |
| isPaid returns true when paid is true | Getter reflects form state |
| minDateTime is at least 30 minutes from now | Correct minimum selectable date |
| minDateTime has correct format (YYYY-MM-DDTHH:mm) | String format check |
| fieldError returns empty for valid field | No error message shown |
| fieldError returns required message | "This field is required." |
| fieldError returns minlength message | "Minimum X characters." |
| fieldError returns maxlength message | "Maximum X characters." |
| fieldError returns pastDate message | "Event date must be in the future." |
| fieldError returns price min message | "Price must be at least 0.5 TND." |
| fieldError returns price max message | "Price cannot exceed 10,000 TND." |
| fieldError returns maxParticipants min message | "Must be at least 1 participant." |
| fieldError returns maxParticipants max message | "Cannot exceed 10,000 participants." |
| isFieldInvalid returns false for valid untouched field | No highlight before interaction |
| isFieldInvalid returns true for invalid touched field | Highlight after user touches |
| isFieldInvalid returns true when submitAttempted | All errors show after submit attempt |
| onSubmit does not call service when form is invalid | Guard against bad data |
| onSubmit sets submitAttempted to true | Triggers all error messages |
| onSubmit calls createEvent and navigates on success | Happy path create flow |
| goBack navigates to club when clubId is set | Correct back navigation |
| goBack navigates to events list when clubId is null | Fallback navigation |

---

### 7. `src/app/features/student/event-feedback/event-feedback.spec.ts` — 24 tests

Tests the star rating component and feedback submission logic.

| Test | What it checks |
|------|----------------|
| should create | Component initializes |
| loads feedback on init and populates reviews | Reviews array is filled after load |
| loading is false after service error | No infinite spinner on failure |
| setRating updates selectedRating | Star click works |
| hoverRating updates hoveredRating | Hover effect works |
| clearHover resets hoveredRating to 0 | Hover removed on mouse leave |
| starClass returns yellow for stars ≤ hoveredRating | Highlight on hover |
| starClass falls back to selectedRating when not hovering | Correct highlight after click |
| displayStarClass returns yellow for filled stars | Filled star color |
| starsArray returns [1, 2, 3, 4, 5] | Always 5 stars |
| submitFeedback sets error when no rating selected | Cannot submit without a star |
| submitFeedback does not call service when no rating | Guard — service not called |
| submitFeedback calls service with correct arguments | Correct eventId, rating, comment |
| submitFeedback resets rating and comment on success | Form cleared after submit |
| submitFeedback shows success message | "Thank you for your feedback!" |
| submitFeedback shows specific error from server | Shows API error message |
| submitFeedback shows generic error fallback | "Failed to submit feedback." |
| deleteMyReview does nothing when myReview is null | Guard against null |
| deleteMyReview calls deleteFeedback with correct id | Correct feedback id sent |
| getInitials returns first letters of each word | "Alice Bob" → "AB" |
| getInitials returns max 2 characters | "Alice Bob Carol" → "AB" |
| getInitials returns "?" for empty name | Edge case |
| getInitials handles single-word name | "Alice" → "A" |
| getAvatarColor always returns a valid color | One of 6 possible colors |
| getAvatarColor is consistent for the same name | Same name always same color |
| getAvatarColor differs for different starting chars | "Alice" ≠ "Bob" color |
| formatDate formats ISO string correctly | Contains year and month name |
| formatDate returns empty string for empty input | Edge case |

---

## Total

| File | Tests |
|------|-------|
| auth.service.spec.ts | 12 |
| event.service.spec.ts | 22 |
| club.service.spec.ts | 14 |
| feedback.service.spec.ts | 10 |
| auth.guard.spec.ts | 6 |
| event-form.spec.ts | 26 |
| event-feedback.spec.ts | 24 |
| **TOTAL** | **114 tests** |

**Result: 139 passing (includes app tests)**
