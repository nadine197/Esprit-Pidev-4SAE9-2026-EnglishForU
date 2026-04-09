# Reporting and Community Contracts

This file is the source of truth for Feature 1 (reporting/helpdesk) and Feature 2 (community thread) API and security contracts.

## Scope Decisions

- Reporting and community APIs are authenticated-only.
- Delivery order is Feature 1 first, then Feature 2.
- Real-time transport is WebSocket/STOMP with HTTP fallback polling.
- Feature 2 backend runs in a dedicated discussion microservice.
- Image posts use local file upload in the first release.

## Role Matrix

| Capability | STUDENT | TUTOR | HELP_DESK | ADMIN | SUPER_ADMIN |
| --- | --- | --- | --- | --- | --- |
| Create report | Yes | Yes | Yes | Yes | Yes |
| List own reports | Yes | Yes | Yes | Yes | Yes |
| List all reports | No | No | Yes | Yes | Yes |
| Update report status | No | No | Yes | Yes | Yes |
| Assign/unassign report | No | No | Yes | Yes | Yes |
| Read own notifications | Yes | Yes | Yes | Yes | Yes |
| Mark own notification as read | Yes | Yes | Yes | Yes | Yes |
| Create discussion post | Planned | Planned | Planned | Planned | Planned |
| Comment/reaction on discussion | Planned | Planned | Planned | Planned | Planned |

## Report State Transitions

Allowed transitions:

- NEW -> TRIAGED
- TRIAGED -> IN_PROGRESS
- IN_PROGRESS -> DONE
- DONE -> CLOSED
- TRIAGED -> CLOSED
- IN_PROGRESS -> CLOSED

Reopen transitions:

- CLOSED -> TRIAGED
- DONE -> IN_PROGRESS

Rules:

- Only HELP_DESK, ADMIN, SUPER_ADMIN may execute transitions.
- Assignment changes are independent of status transitions.
- Invalid transitions return `400 BAD_REQUEST` with code `INVALID_REPORT_TRANSITION`.

## Notification Event Catalog

Current event types:

- REPORT_CREATED
- REPORT_ASSIGNED
- REPORT_STATUS_CHANGED
- REPORT_COMMENT_ADDED
- DISCUSSION_POST_CREATED
- DISCUSSION_COMMENT_ADDED
- DISCUSSION_REACTION_ADDED
- SYSTEM_ALERT

Notification payload contract:

- `type`: enum value from `NotificationType`.
- `title`: short summary for dropdown/card.
- `message`: user-facing details.
- `link`: frontend route to open on click.
- `reportId`: optional report reference (present for reporting module events).

## Feature 1 HTTP Contracts

Reporter APIs:

- `POST /api/reports`
- `GET /api/reports/mine`

Helpdesk APIs:

- `GET /api/helpdesk/reports?status={optional}`
- `PATCH /api/helpdesk/reports/{id}`

Notification APIs:

- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `POST /api/notifications/{id}/read`

Gateway mirrors under the same paths and forwards auth headers.

## Feature 2 Planned HTTP Contracts

Discussion feed:

- `GET /api/discussions/feed`
- `POST /api/discussions/posts`
- `GET /api/discussions/posts/{postId}`
- `POST /api/discussions/posts/{postId}/comments`
- `POST /api/discussions/posts/{postId}/reactions`

Filtering:

- `GET /api/discussions/feed?scope=mine|others|all&level=A1|A2|B1|B2|C1|C2&courseId={id}`

Media:

- `POST /api/discussions/posts/{postId}/image`

## Real-time Contract (STOMP Baseline)

Planned topic strategy:

- `CONNECT /ws` with `Authorization: Bearer <jwt>` STOMP native header.
- `/topic/notifications/{userId}` for direct user notification events.
- `/topic/helpdesk/reports` for board-level ticket changes.
- `/topic/discussions/{courseId}` for community feed updates.

Security:

- WebSocket handshake requires JWT auth.
- Subscription authorization must validate user role and audience scope.

## Error Contract

All APIs follow these status rules:

- `401 UNAUTHORIZED` for missing/invalid token.
- `403 FORBIDDEN` for authenticated but unauthorized role.
- `404 NOT_FOUND` for missing resources.
- `400 BAD_REQUEST` for validation or transition violations.
- `500` reserved for unexpected server failures.
